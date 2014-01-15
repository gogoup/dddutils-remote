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

package org.gogoup.dddutils.pool.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gogoup.dddutils.pool.MappedPool;
import org.gogoup.dddutils.pool.MappedPoolDelegate;
import org.gogoup.dddutils.pool.Pool;
import org.gogoup.dddutils.pool.PoolDelegate;
import org.gogoup.dddutils.pool.PoolTimeoutException;

public abstract class AutoFilledMappedPool<K, T> implements MappedPool<K, T> {
    
    private static final int DEFAULT_CACHE_CAPACITY = 100;
    
    private int mappedPoolMinCapacity;
    private int mappedPoolMaxCapacity;
    private int mappedPoolIncrement;
    private Map<K, MappablePool<K, T>> pools;
    private MappedPoolDelegate<K, T> delegate;
    
    public AutoFilledMappedPool(int mappedPoolMinCapacity, int mappedPoolMaxCapacity, 
            int mappedPoolIncrement, MappedPoolDelegate<K, T> delegate) {
        this(mappedPoolMinCapacity, mappedPoolMaxCapacity, mappedPoolIncrement, 
                delegate, DEFAULT_CACHE_CAPACITY);
    }
    
    public AutoFilledMappedPool(int mappedPoolMinCapacity, int mappedPoolMaxCapacity, 
            int mappedPoolIncrement, MappedPoolDelegate<K, T> delegate, int cacheCapacity) {
        
        if (mappedPoolMaxCapacity == 0) {
            throw new IllegalArgumentException("Max capacity need to be greater than 0.");
        }
            
        if (mappedPoolIncrement <= 0) {
            throw new IllegalArgumentException("Increment need to be greater than 0.");
        }
            
        if (mappedPoolIncrement > mappedPoolMaxCapacity) {
            throw new IllegalArgumentException("Increment cannot exceed max capacity, "
                    + "(" + mappedPoolIncrement + " > " + mappedPoolMaxCapacity + ").");
        }
            
        if (mappedPoolMinCapacity > mappedPoolMaxCapacity) {
            throw new IllegalArgumentException("Min capacity cannot exceed max capacity,"
                    + " (" + mappedPoolMinCapacity + " > " + mappedPoolMaxCapacity + ").");
        }
        
        this.mappedPoolMinCapacity = mappedPoolMinCapacity;
        this.mappedPoolMaxCapacity = mappedPoolMaxCapacity;
        this.mappedPoolIncrement = mappedPoolIncrement;
//        this.pools = new HashMap<K, MappablePool<K, T>>();
        this.pools = new LRUPoolCache<K, MappablePool<K, T>>(cacheCapacity);
        this.setDelegate(delegate);
    }
    
    @Override
    public void setDelegate(MappedPoolDelegate<K, T> delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public MappedPoolDelegate<K, T> getDelegate() {
        return delegate;
    }
    
    @Override
    public int getCapacity(K key) {
        return this.pools.get(key).getCapacity();
    }
    
    @Override
    public int getSize(K key) {
        return this.pools.get(key).getSize();
    }
    
    @Override
    public boolean pourItem(K key, T item) {
        
        MappablePool<K, T> pool = getPoolWithoutNull(key);
        return pool.addItem(item);
    }
    
    @Override
    public boolean pullItem(K key, T item) {
        MappablePool<K, T> pool = this.getPool(key);
        return null == pool ? false : pool.removeItem(item);
    }
    
    @Override
    public void remove(K key) {
        synchronized (this.pools) {
            this.pools.remove(key);
        }
    }
    
    @Override
    public T borrowItem(K key) {
        // System.out.println("HERE======>AutoFilledMappedPool.borrowItem() #1 KEY: "+key+"; THREAD: "+Thread.currentThread().getId());
        MappablePool<K, T> pool = getPoolWithoutNull(key);
        // System.out.println("HERE======>AutoFilledMappedPool.borrowItem() #2 POOL: "+pool+"; THREAD: "+Thread.currentThread().getId());
        T item = pool.borrowItem();
        // System.out.println("HERE======>AutoFilledMappedPool.borrowItem() #3 ITEM: "+item+"; THREAD: "+Thread.currentThread().getId());
        return item;
    }
    
    @Override
    public T borrowItem(K key, long timeout) throws PoolTimeoutException {
        MappablePool<K, T> pool = getPoolWithoutNull(key);
        return pool.borrowItem(timeout);
    }
    
    @Override
    public void returnItem(K key, T item) {
        // System.out.println("HERE======>AutoFilledMappedPool.returnItem() #1 KEY: "+key+"; THREAD: "+Thread.currentThread().getId());
        MappablePool<K, T> pool = this.getPool(key);
        // System.out.println("HERE======>AutoFilledMappedPool.returnItem() #2 KEY: "+key+"; THREAD: "+Thread.currentThread().getId());
        if (null != pool)
            pool.returnItem(item);
        // System.out.println("HERE======>AutoFilledMappedPool.returnItem() #3 KEY: "+key+"; THREAD: "+Thread.currentThread().getId());
        
    }
    
    private MappablePool<K, T> getPoolWithoutNull(K key) {
        synchronized (this) {
            // System.out.println("HERE======>AutoFilledMappedPool.getPoolWithoutNull() #1 KEY: "+key+"; THREAD: "+Thread.currentThread().getId());
            MappablePool<K, T> pool = this.pools.get(key);
            if (null == pool) {
                MappablePoolDelegate<K, T> poolDelegate = new MappablePoolDelegate<K, T>(key, this);
                pool = new MappablePool<K, T>(key, this, this.mappedPoolMinCapacity, this.mappedPoolMaxCapacity, this.mappedPoolIncrement,
                        poolDelegate);
                this.pools.put(key, pool);
            }
            // System.out.println("HERE======>AutoFilledMappedPool.getPoolWithoutNull() #2 KEY: "+key+"; THREAD: "+Thread.currentThread().getId());
            return pool;
        }
    }
    
    private MappablePool<K, T> getPool(K key) {
        synchronized (this) {
            return this.pools.get(key);
        }
    }
    
    /**
     * Returns a created item T with the specified key K.
     * 
     * NOTE: This method is not thread-safe. The implementation of this method
     * requires necessary multi-threads synchornization.
     * 
     * @param key
     * @return
     */
    abstract protected T fill(K key);
    
    /**
     * Returns a created item T with the specified key K in the giving time.
     * 
     * NOTE: This method is not thread-safe. The implementation of this method
     * requires necessary multi-threads synchornization.
     * 
     * @param key
     * @param timeout
     * @return
     * @throws PoolTimeoutException
     */
    abstract protected T fill(K key, long timeout) throws PoolTimeoutException;
    
    
    private static class LRUPoolCache <K, V> extends LinkedHashMap <K, V> {
        
        /**
         * 
         */
        private static final long serialVersionUID = -4866520469042526831L;
        private int capacity; 
         
        public LRUPoolCache(int capacity) { 
            super(capacity+1, 1.0f, true); // Pass 'true' for accessOrder.
            this.capacity = capacity;
        }
         
        protected boolean removeEldestEntry(Map.Entry<K,V> entry) {          
            return (size() > this.capacity);
        } 
    }    
    
    private static class MappablePool<K, T> extends AutoFilledPool<T> {
        
        private K key;
        private AutoFilledMappedPool<K, T> mainPool;
        
        public MappablePool(K key, AutoFilledMappedPool<K, T> pool, int minCapacity, int maxCapacity, int increment,
                PoolDelegate<T> delegate) {
            super(minCapacity, maxCapacity, increment, delegate);
            
            this.key = key;
            this.mainPool = pool;
        }
        
        @Override
        protected T fill() {
            return this.mainPool.fill(this.key);
        }
        
        @Override
        protected T fill(long timeout) throws PoolTimeoutException {
            return this.mainPool.fill(this.key, timeout);
        }
        
    }
    
    private static class MappablePoolDelegate<K, T> implements PoolDelegate<T> {
        
        private K key;
        private AutoFilledMappedPool<K, T> pool;
        
        public MappablePoolDelegate(K key, AutoFilledMappedPool<K, T> pool) {
            this.key = key;
            this.pool = pool;
        }
        
        @Override
        public boolean willBorrow(Pool<T> pool, T item) {
            if (null == this.pool.getDelegate())
                return true;
            return this.pool.getDelegate().willBorrow(this.pool, this.key, item);
        }
        
        @Override
        public boolean willReturn(Pool<T> pool, T item) {
            if (null == this.pool.getDelegate())
                return true;
            return this.pool.getDelegate().willReturn(this.pool, this.key, item);
        }
        
    }
}
