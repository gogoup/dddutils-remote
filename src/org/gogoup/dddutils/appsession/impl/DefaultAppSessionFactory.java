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

import org.gogoup.dddutils.appsession.AppServiceResolver;
import org.gogoup.dddutils.appsession.AppSession;
import org.gogoup.dddutils.appsession.AppSessionContext;
import org.gogoup.dddutils.appsession.AppSessionFactory;
import org.gogoup.dddutils.appsession.AppSessionFactoryDelegate;
import org.gogoup.dddutils.appsession.NoSuchAppSessionException;
import org.gogoup.dddutils.pool.MappedPool;
import org.gogoup.dddutils.pool.MappedPoolDelegate;
import org.gogoup.dddutils.pool.ParameterResolver;
import org.gogoup.dddutils.pool.PoolTimeoutException;
import org.gogoup.dddutils.pool.impl.AutoFilledMappedPool;

public class DefaultAppSessionFactory implements AppSessionFactory{

	private String applicationId;
	private AppSessionPool sessionPool;
	private AppSessionFactoryDelegate delegate;
	
	public DefaultAppSessionFactory(String applicationId, int minCapacity, 
			int maxCapacity, int increment, AppSessionFactoryDelegate delegate){
		
		this.sessionPool = new AppSessionPool(this, minCapacity, maxCapacity, 
				increment, new DefaultAppSessionPoolDelegate(this));
		
		this.applicationId=applicationId;
		if(null == delegate) 
			throw new IllegalArgumentException("AppSessionFactoryDelegate required!");
		this.setDelegate(delegate);
	}
	
	@Override
	public String getApplicationId() {
		return this.applicationId;
	}
	
	@Override
	public AppSession getSession(String key) throws NoSuchAppSessionException {
		if(null == key)
			throw new IllegalArgumentException("Session key cannot be null value.");
		//System.out.println("HERE======>DefaultAppSessionFactory.getSession() #1 KEY: "+key+"; THREAD: "+Thread.currentThread().getId());
		AppSession session = this.sessionPool.borrowItem(key);
		//System.out.println("HERE======>DefaultAppSessionFactory.getSession() #2 SESSION: "+session+"; THREAD: "+Thread.currentThread().getId());
		//session.update();
		//System.out.println("HERE======>DefaultAppSessionFactory.getSession() #3 SESSION: "+session+"; THREAD: "+Thread.currentThread().getId());
		return session;
		
	}


	@Override
	public AppSessionFactoryDelegate getDelegate() {
		return this.delegate;
	}

	@Override
	public void setDelegate(AppSessionFactoryDelegate delegate) {
		this.delegate = delegate;
	}

	void returnSession(DefaultAppSession session) {
		this.sessionPool.returnItem(session.getSessionKey(), session);
		
	}
	
	void removeSession(String sessionKey) {
		this.sessionPool.remove(sessionKey);
	}
	
	private DefaultAppSession assembleAppSession(String sessionKey) {
		//System.out.println("HERE======>DefaultAppSessionFactory.assembleAppSession() #1");
		//session
		DefaultAppSession session = (DefaultAppSession) this.delegate.assembleAppSession(this, sessionKey);
		//System.out.println("HERE======>DefaultAppSessionFactory.assembleAppSession() #2 "+this.delegate);
		//register parameter resolvers
		ParameterResolver[] paramResolvers = this.delegate.loadParameterResolvers(this, session);
		//System.out.println("HERE======>DefaultAppSessionFactory.assembleAppSession() #3");
		if(null == paramResolvers)
			paramResolvers = new ParameterResolver[0];
		for(ParameterResolver resolver: paramResolvers)
		{
			//System.out.println("HERE======>DefaultAppSessionFactory.assembleAppSession() paramResolver: "+resolver);
			session.getParameterPool().registerParameterResolver(resolver);
		}
		//session context
		AppSessionContext context = new DefaultAppSessionContext(this, session);
		//System.out.println("HERE======>DefaultAppSessionFactory.assembleAppSession() #4");
		//application service resolvers
		AppServiceResolver[] servResolvers = this.delegate.loadAppServiceResolvers(this, context);
		//System.out.println("HERE======>DefaultAppSessionFactory.assembleAppSession() #5");
		if(null == servResolvers)
			servResolvers = new AppServiceResolver[0];		
		for(AppServiceResolver resolver: servResolvers)
			context.registerAppServiceResolver(resolver);
		
		return session;
	}
	
	private static class AppSessionPool extends AutoFilledMappedPool<String, DefaultAppSession> {

		private DefaultAppSessionFactory sessionFactory;
		
		public AppSessionPool(DefaultAppSessionFactory manager, int minCapacity, int maxCapacity, int increment,
				MappedPoolDelegate<String, DefaultAppSession> delegate) {
			super(minCapacity, maxCapacity, increment, delegate);
			this.sessionFactory=manager;
		}

		@Override
		protected DefaultAppSession fill(String key) {
			return sessionFactory.assembleAppSession(key);
		}

		@Override
		protected DefaultAppSession fill(String key, long timeout)
				throws PoolTimeoutException {
			return this.fill(key);
		}
		
	}
	
	private static class DefaultAppSessionPoolDelegate implements MappedPoolDelegate<String, DefaultAppSession> {
		
		//private DefaultAppSessionFactory sessionFactory;
		
		public DefaultAppSessionPoolDelegate(DefaultAppSessionFactory sessionFactory) {
			//this.sessionFactory=sessionFactory;
		}

		@Override
		public boolean willBorrow(MappedPool<String, DefaultAppSession> pool,
				String key, DefaultAppSession item) {
			//System.out.println("HERE======>DefaultAppSessionPoolDelegate.willBorrow() #1");
			return !item.isExpiry();
		}

		@Override
		public boolean willReturn(MappedPool<String, DefaultAppSession> pool,
				String key, DefaultAppSession item) {
			//System.out.println("HERE======>DefaultAppSessionPoolDelegate.willReturn() #1");
			return !item.isExpiry();
		}
		
	}
	
	
	private static class DefaultAppSessionContext implements AppSessionContext {
		
		private AppSessionFactory factory;
		private DefaultAppSession session;
		
		public DefaultAppSessionContext(AppSessionFactory factory, DefaultAppSession session) {
			this.factory=factory;
			this.session=session;
		}

		@Override
		public void registerParameterResolver(ParameterResolver resolver) {
			this.session.getParameterPool().registerParameterResolver(resolver);
		}
		
		@Override
		public void registerAppServiceResolver(AppServiceResolver resolver) {			
			this.session.getParameterPool().registerParameterResolver(resolver);
		}	
		
		@Override
		public AppSession getCurrentSession() {
			return session;
		}

		@Override
		public AppSession getSession(String key) throws NoSuchAppSessionException {
			return this.factory.getSession(key);
		}

	}
}
