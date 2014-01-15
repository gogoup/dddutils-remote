/*
 * Copyright 2013 Rui Sun (SteveSunCanada@gmail.com)
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.gogoup.dddutils.objectsegment;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gogoup.dddutils.pool.ParameterPool;
import org.gogoup.dddutils.pool.resolver.TransientParameterResolver;

public abstract class OSTransactionalResolver extends TransientParameterResolver {
    
    public static final String PREPARE_TRANSACTION = "_START_TRANSACTION_";
    public static final String COMMIT_TRANSACTION = "_COMMIT_TRANSACTION_";
    public static final String ROLLBACK_TRANSACTION = "_ROLLBACK_TRANSACTION_";
    public static final String IS_COMMITTABLE = "_IS_COMMITTABLE_";
    public static final String APP_TRANSACTION = "_APP_TRANSACTION_";
    
    private String[] names;
    
    private boolean isAppTransactionPrepared;
    private Map<String, Object> preparedTransactions;
    /*
     * to count the number of how many times of "PREPARE_TRANSATION" for each of
     * transactions.
     */
    private Map<String, Integer> preparedTransactionCounters;
    private Set<String> rollbackTransactionIds;
    
    public OSTransactionalResolver(String[] names) {
        super(null);
        
        // reassemble the new names array.
        this.names = new String[names.length + 4];
        this.names[0] = OSTransactionalResolver.PREPARE_TRANSACTION;
        this.names[1] = OSTransactionalResolver.COMMIT_TRANSACTION;
        this.names[2] = OSTransactionalResolver.ROLLBACK_TRANSACTION;
        this.names[3] = OSTransactionalResolver.IS_COMMITTABLE;
        System.arraycopy(names, 0, this.names, 4, names.length);
        
        this.isAppTransactionPrepared = false;
        this.preparedTransactions = new LinkedHashMap<String, Object>();
        this.preparedTransactionCounters = new LinkedHashMap<String, Integer>();
        this.rollbackTransactionIds = new LinkedHashSet<String>();
    }
    
    abstract protected Object startNonTransaction(String id);
    
    abstract protected Object startTransaction(String id);
    
    abstract protected void commitTransaction(String id, Object transaction);
    
    abstract protected void rollbackTransaction(String id, Object transaction);
    
    @Override
    public String[] getParameterNames(ParameterPool pool) {
        return names;
    }
    
    @Override
    public Object getParameter(ParameterPool pool, String name) {
        
        if (OSTransactionalResolver.IS_COMMITTABLE.equals(name))
            return (this.rollbackTransactionIds.size() == 0);
        
        String id = name;
        
        Object transaction = this.preparedTransactions.get(id);
        if (null != transaction)
            return transaction;
        
        // if there is non transactions started.
        if (!this.isAppTransactionPrepared && !this.preparedTransactions.containsKey(id))
            return this.startNonTransaction(id);
        
        transaction = this.startTransaction(id);
        // register the started transaction
        this.preparedTransactions.put(id, transaction);
        
        return transaction;
    }
    
    @Override
    public boolean setParameter(ParameterPool pool, String name, Object value) {
        
        if (OSTransactionalResolver.PREPARE_TRANSACTION.equals(name)) {
            if (null == value || OSTransactionalResolver.APP_TRANSACTION.equals(value)) {
                
                if (!this.isAppTransactionPrepared && this.preparedTransactions.size() > 0)
                    throw new RuntimeException("Need to finish other transactions before preparing an app transaction.");
                
                this.isAppTransactionPrepared = true;
                // +1
                increaseCounter(OSTransactionalResolver.APP_TRANSACTION, this.preparedTransactionCounters);
                
                return true;
            }
            
            String id = (String) value;
            
            // prepare the specified transaction for the giving name
            if (!this.preparedTransactions.containsKey(id))
                this.preparedTransactions.put(id, null);
            
            // start counting for the specified transaction if there is no app
            // transaction prepared
            if (!this.isAppTransactionPrepared) {
                // +1
                increaseCounter(id, this.preparedTransactionCounters);
            }
            
            return true;
        }
        if (OSTransactionalResolver.COMMIT_TRANSACTION.equals(name)) {
            // for app transaction
            if (null == value || OSTransactionalResolver.APP_TRANSACTION.equals(value)) {
                if (!this.isAppTransactionPrepared)
                    throw new RuntimeException("App transaction has not prepared yet.");
                
                // -1
                decreaseCounter(OSTransactionalResolver.APP_TRANSACTION, this.preparedTransactionCounters);
                // commit all holding transaction at once the app transaction
                // counter decrease to 0
                if (0 == readCounter(OSTransactionalResolver.APP_TRANSACTION, this.preparedTransactionCounters)) {
                    this.validateForCommitable();
                    this.commitAll();
                }
                
                return true;
            }
            
            /*
             * for one specific transction
             */
            String id = (String) value;
            
            if (!this.preparedTransactions.containsKey(id) || null == this.preparedTransactions.get(id))
                throw new RuntimeException("Transaction, " + id + " has not prepared yet.");
            
            // decrease the counter for the specified transaction if there is no
            // app transaction prepared
            if (!this.isAppTransactionPrepared) {
                // -1
                decreaseCounter(id, this.preparedTransactionCounters);
                // commit the transaction right away as long as the counter
                // decrease to 0.
                if (0 == readCounter(id, this.preparedTransactionCounters)) {
                    this.validateForCommitable();
                    this.commitTransaction(id, this.preparedTransactions.get(id));
                    this.preparedTransactions.remove(id);
                    this.preparedTransactionCounters.remove(id);
                }
            }
            
            return true;
        }
        
        if (OSTransactionalResolver.ROLLBACK_TRANSACTION.equals(name)) {
            // for app transaction
            if (null == value || OSTransactionalResolver.APP_TRANSACTION.equals(value)) {
                if (!this.isAppTransactionPrepared)
                    throw new RuntimeException("App transaction has not prepared yet.");
                // -1
                decreaseCounter(OSTransactionalResolver.APP_TRANSACTION, this.preparedTransactionCounters);
                // rollback all holding transaction at once the app transaction
                // counter decrease to 0
                if (0 == readCounter(OSTransactionalResolver.APP_TRANSACTION, this.preparedTransactionCounters))
                    this.rollbackAll();
                
                return true;
            }
            
            /*
             * for one specific transction
             */
            String id = (String) value;
            
            if (!this.preparedTransactions.containsKey(id) || null == this.preparedTransactions.get(id))
                throw new RuntimeException("Transaction, " + id + " has not prepared yet.");
            
            // record the rollback transaction.
            this.rollbackTransactionIds.add(id);
            
            // decrease the counter for the specified transaction if there is no
            // app transaction prepared
            if (!this.isAppTransactionPrepared) {
                // -1
                decreaseCounter(id, this.preparedTransactionCounters);
                // commit the transaction right away as long as the counter
                // decrease to 0.
                if (0 == readCounter(id, this.preparedTransactionCounters)) {
                    // rollback the transaction right away.
                    this.rollbackTransaction(id, this.preparedTransactions.get(id));
                    this.preparedTransactions.remove(id);
                    this.preparedTransactionCounters.remove(id);
                    this.rollbackTransactionIds.remove(id);
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean isReadOnly(ParameterPool pool, String name) {
        if (OSTransactionalResolver.PREPARE_TRANSACTION.equals(name))
            return false;
        if (OSTransactionalResolver.COMMIT_TRANSACTION.equals(name))
            return false;
        if (OSTransactionalResolver.ROLLBACK_TRANSACTION.equals(name))
            return false;
        return true;
    }
    
    @Override
    public void reset(ParameterPool pool) {
        
        this.rollbackAll();
    }
    
    @Override
    public void clear(ParameterPool pool) {
        
        if (this.isAppTransactionPrepared || this.preparedTransactions.size() > 0)
            throw new RuntimeException("Unfinished transaction(s) found!");
        
        this.commitAll();
    }
    
    private void commitAll() {
        
        if (this.rollbackTransactionIds.size() > 0)
            throw new RuntimeException(this.rollbackTransactionIds.size() + " transactions rolled back. "
                    + "Suggest to apply IS_COMMITTABLE to check before perform this level of commit operation.");
        
        for (Iterator<String> iter = this.preparedTransactions.keySet().iterator(); iter.hasNext();) {
            String id = iter.next();
            
            Object transaction = this.preparedTransactions.get(id);
            if (null == transaction)
                continue;
            
            this.commitTransaction(id, transaction);
        }
        
        this.rollbackTransactionIds.clear();
        this.preparedTransactions.clear();
        this.preparedTransactionCounters.clear();
        this.isAppTransactionPrepared = false;
        
    }
    
    private void rollbackAll() {
        
        for (Iterator<String> iter = this.preparedTransactions.keySet().iterator(); iter.hasNext();) {
            String id = iter.next();
            
            Object transaction = this.preparedTransactions.get(id);
            if (null == transaction)
                continue;
            
            this.rollbackTransaction(id, transaction);
        }
        
        this.rollbackTransactionIds.clear();
        this.preparedTransactions.clear();
        this.preparedTransactionCounters.clear();
        this.isAppTransactionPrepared = false;
        
    }
    
    private void validateForCommitable() {
        if (this.rollbackTransactionIds.size() > 0)
            throw new RuntimeException("Rollback transaction(s) found. "
                    + "Suggest to apply 'IS_COMMITTABLE' to check before perform commit operation.");
    }
    
    private static void increaseCounter(String name, Map<String, Integer> counters) {
        if (!counters.containsKey(name)) {
            counters.put(name, 1);
            return;
        }
        int count = counters.get(name);
        counters.put(name, ++count);
    }
    
    private static void decreaseCounter(String name, Map<String, Integer> counters) {
        if (!counters.containsKey(name))
            throw new RuntimeException("No such counter found with the giving name, " + name);
        
        int count = counters.get(name);
        count--;
        if (count < 0)
            throw new RuntimeException("Count " + name + "'s value is out of boundary, " + count);
        counters.put(name, count);
    }
    
    private static int readCounter(String name, Map<String, Integer> counters) {
        if (!counters.containsKey(name))
            throw new RuntimeException("No such counter found with the giving name, " + name);
        
        return counters.get(name);
        
    }
    
}
