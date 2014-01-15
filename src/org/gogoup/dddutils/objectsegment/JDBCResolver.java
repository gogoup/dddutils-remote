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

import java.sql.Connection;
import java.sql.SQLException;

import org.gogoup.dddutils.pool.ParameterPool;

public class JDBCResolver extends OSTransactionalResolver {
    
    private DBConnectionManager dbConnectionManager;
    
    public JDBCResolver(DBConnectionManager connManager, String[] connNames) {
        super(connNames);
        // initial connection manager
        this.dbConnectionManager = connManager;
        
    }
    
    @Override
    protected Object startNonTransaction(String id) {
        DBConnectionWrapper connection = new DBConnectionWrapper(
                (Connection) this.dbConnectionManager.getConnection(id), false);
        return connection;
    }
    
    @Override
    protected Object startTransaction(String id) {
        
        DBConnectionWrapper connection = new DBConnectionWrapper(
                (Connection) this.dbConnectionManager.getConnection(id), true);
        
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return connection;
    }
    
    @Override
    protected void commitTransaction(String id, Object transaction) {
        
        DBConnectionWrapper connection = (DBConnectionWrapper) transaction;
        
        try {
            if (!connection.isClosed()) {
                connection.commit();
                connection.close();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            
        }
        
    }
    
    @Override
    protected void rollbackTransaction(String id, Object transaction) {
        
        DBConnectionWrapper connection = (DBConnectionWrapper) transaction;
        try {
            
            if (!connection.isClosed()) {
                connection.rollback();
                connection.close();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            
        }
        
    }
    
    @Override
    public String[] getDependences(ParameterPool pool, String name) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public boolean verifyDependence(ParameterPool pool, String name, Object parameter) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public String[] findParameterNames(ParameterPool pool, String query) {
        return null;
    }
    
}
