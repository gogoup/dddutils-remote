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

package org.gogoup.dddutils.appsession.impl;

import java.util.Iterator;
import java.util.Map;

import org.gogoup.dddutils.appsession.AppServiceResolver;
import org.gogoup.dddutils.appsession.AppSession;
import org.gogoup.dddutils.appsession.AppSessionFactory;
import org.gogoup.dddutils.appsession.AppSessionFactoryDelegate;
import org.gogoup.dddutils.appsession.NoSuchAppSessionException;
import org.gogoup.dddutils.pool.MappedPool;
import org.gogoup.dddutils.pool.MappedPoolDelegate;
import org.gogoup.dddutils.pool.ParameterPool;
import org.gogoup.dddutils.pool.ParameterResolver;
import org.gogoup.dddutils.pool.PoolTimeoutException;
import org.gogoup.dddutils.pool.impl.AutoFilledMappedPool;
import org.gogoup.dddutils.pool.impl.DefaultParameterPool;

public class DefaultAppSessionFactory implements AppSessionFactory {
    
    private AppSessionFactoryConfig config;    
    private MappedPool<String, DefaultAppSession> sessionPool;
    private AppSessionFactoryDelegate delegate;    
    private ParameterPool applicationParameterPool;
    
    public DefaultAppSessionFactory(AppSessionFactoryConfig config,
            AppSessionFactoryDelegate delegate) {        
        this(config, delegate, 
                new AppSessionPoolDelegate());
    }
    
    public DefaultAppSessionFactory(AppSessionFactoryConfig config, AppSessionFactoryDelegate delegate,
            MappedPoolDelegate<String, DefaultAppSession> sessionPoolDelegate) {
        
        this.config = config;        
        this.sessionPool = new AppSessionPool(this, 1, this.config.getConcurrentSessionSize(), 
                1, sessionPoolDelegate, this.config.getSessionCacheSize());
        this.applicationParameterPool = new DefaultParameterPool();
        
        this.setSessionPoolDelegate(sessionPoolDelegate);
        this.setDelegate(delegate);
        this.initApplicationParameters(this.config.getApplicationParameters());
    }
    
    @Override
    public String getApplicationId() {
        return config.getApplicationId();
    }
    
    @Override
    public AppSession getSession(String key) throws NoSuchAppSessionException {
        if (null == key)
            throw new IllegalArgumentException("Session key cannot be null value.");
        // System.out.println("HERE======>DefaultAppSessionFactory.getSession() #1 KEY: "+key+"; THREAD: "+Thread.currentThread().getId());
        AppSession session = this.sessionPool.borrowItem(key);
        // System.out.println("HERE======>DefaultAppSessionFactory.getSession() #2 SESSION: "+currentSession+"; THREAD: "+Thread.currentThread().getId());
        // currentSession.update();
        // System.out.println("HERE======>DefaultAppSessionFactory.getSession() #3 SESSION: "+currentSession+"; THREAD: "+Thread.currentThread().getId());
        return session;
    }
    
    @Override
    public AppSessionFactoryDelegate getDelegate() {
        return this.delegate;
    }
    
    @Override
    public void setDelegate(AppSessionFactoryDelegate delegate) {    
        this.delegate = delegate;
        if (null != this.delegate) {
            this.initApplicationParameterResolvers();
        }
    }
    
    @Override
    public void close() {
        this.applicationParameterPool.clear();
    }

    protected void setSessionPoolDelegate(MappedPoolDelegate<String, DefaultAppSession> delegate) {
        this.sessionPool.setDelegate(delegate);
    }
    
    void returnSession(DefaultAppSession session) {
        this.sessionPool.returnItem(session.getSessionKey(), session);
        
    }
    
    void removeSession(String sessionKey) {
        this.sessionPool.remove(sessionKey);
    }
    
    Object getApplicationParameter(String name) {
        synchronized(this) {
            //System.out.println("HERE======>DefaultAppSesssionFactory.getApplicationParameter() Name: "+name);
            //System.out.println("HERE======>DefaultAppSesssionFactory.getApplicationParameter() Value: "+this.applicationParameterPool.getParameter(name));
            return this.applicationParameterPool.getParameter(name);
        }
        
    }

    void setApplicationParameter(String name, Object value) {
        synchronized(this) {
            this.applicationParameterPool.setParameter(name, value);
        }
    }
    
    long registerParameterResolver(ParameterResolver resolver) {
        synchronized(this) {
            return this.applicationParameterPool.registerParameterResolver(resolver);
        }
    }

    void deregisterParameterResolver(long id) {
        synchronized(this) {
            this.applicationParameterPool.deregisterParameterResolver(id);
        }
    }
    
    protected synchronized DefaultAppSession assembleAppSession(String sessionKey) {
  
        DefaultAppSession session = new DefaultAppSession(sessionKey, this);
        
        // register parameter resolvers
        ParameterResolver[] paramResolvers = this.delegate.registerParameterResolvers(this, session);
        
        if (null == paramResolvers)
            paramResolvers = new ParameterResolver[0];
        for (ParameterResolver resolver : paramResolvers) {
            session.registerSessionParameterResolver(resolver);
        }
        
        // application service resolvers
        AppServiceResolver[] servResolvers = this.delegate.registerAppServiceResolvers(this, session);
        
        if (null == servResolvers) {
            servResolvers = new CachedAppServiceResolver[0];
        }
        
        for (AppServiceResolver resolver : servResolvers) {
            CachedAppServiceResolver cachedResolver = new CachedAppServiceResolver(session, resolver);
            session.registerSessionParameterResolver(cachedResolver);
        }
        
        return session;
    }
    
    private void initApplicationParameterResolvers() {
        ParameterResolver[] paramResolvers = this.getDelegate().registerApplicationParameterResolvers(this);
        if (null == paramResolvers) {
            paramResolvers = new ParameterResolver[0];
        }
        for (ParameterResolver resolver : paramResolvers) {
            applicationParameterPool.registerParameterResolver(resolver);
        }
    }
    
    private void initApplicationParameters(Map<String, Object> parameters) {
        for (Iterator<String> iter = parameters.keySet().iterator(); iter.hasNext();) {
            String name = iter.next();
            //System.out.println("HERE======>DefaultAppSesssionFactory.initParameters() Name: "+name);
            Object param = parameters.get(name);
            //System.out.println("HERE======>DefaultAppSesssionFactory.initParameters() Value: "+param);
            applicationParameterPool.setParameter(name, param);
        }
    }

    /**
     * This is a concrete class of AutoFilledMappedPool.
     * 
     * <tt>The implementation of methods {@link #fill(String)} and {@link #fill(String, long)} is thread-safety.</tt>
     * 
     * @author sunr
     * 
     */
    private static class AppSessionPool extends AutoFilledMappedPool<String, DefaultAppSession> {
        
        private DefaultAppSessionFactory sessionFactory;
        
        public AppSessionPool(DefaultAppSessionFactory manager, int mappedPoolMinCapacity, 
                int mappedPoolMaxCapacity, int mappedPoolIncrement,
                MappedPoolDelegate<String, DefaultAppSession> delegate, int cacheCapacity) {
            super(mappedPoolMinCapacity, mappedPoolMaxCapacity, mappedPoolIncrement, delegate, cacheCapacity);
            this.sessionFactory = manager;
        }
        
        @Override
        protected DefaultAppSession fill(String key) {
            return sessionFactory.assembleAppSession(key);
        }
        
        @Override
        protected DefaultAppSession fill(String key, long timeout) throws PoolTimeoutException {
            return this.fill(key);
        }
        
    }
    
    private static class AppSessionPoolDelegate implements MappedPoolDelegate<String, DefaultAppSession> {
        
        public AppSessionPoolDelegate() {
        }
        
        @Override
        public boolean willBorrow(MappedPool<String, DefaultAppSession> pool, String key, DefaultAppSession item) {
            // System.out.println("HERE======>DefaultAppSessionPoolDelegate.willBorrow() #1");
            return true;
        }
        
        @Override
        public boolean willReturn(MappedPool<String, DefaultAppSession> pool, String key, DefaultAppSession item) {
            // System.out.println("HERE======>DefaultAppSessionPoolDelegate.willReturn() #1");
            return true;
        }
        
    }
    
}
