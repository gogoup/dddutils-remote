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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.gogoup.dddutils.appsession.AppSession;
import org.gogoup.dddutils.appsession.AppSessionFactory;
import org.gogoup.dddutils.appsession.InconsistentAppSessionException;
import org.gogoup.dddutils.appsession.NoSuchAppSessionException;
import org.gogoup.dddutils.appsession.spi.AppSessionOS;
import org.gogoup.dddutils.appsession.spi.AppSessionSPI;
import org.gogoup.dddutils.objectsegment.ObjectSegment;
import org.gogoup.dddutils.pool.ParameterPool;
import org.gogoup.dddutils.pool.impl.DefaultParameterPool;

public class DefaultAppSession implements AppSession {
	
	private static final int STATUS_RESET = 0;
	private static final int STATUS_UPDATED = 1;
	private static final int STATUS_DESTORYED = 2;
	
	private DefaultAppSessionFactory factory;
	private ParameterPool parameterPool;
	private String sessionKey;
	private AppSessionOS objectSegment;
	private AppSessionSPI spi;	
	private int status;
	
	public DefaultAppSession(DefaultAppSessionFactory factory, String sessionKey, ParameterPool parameterPool) {
		
		this.sessionKey=sessionKey;
		this.factory=factory;
		this.parameterPool=parameterPool;
		this.objectSegment=null;
		this.reset();
	}
	
	public DefaultAppSession(DefaultAppSessionFactory factory, String sessionKey) {
		this(factory, sessionKey, new DefaultParameterPool());
	}
	
	public ObjectSegment getObjectSegment() {return this.objectSegment;}
	
	public ParameterPool getParameterPool() {return this.parameterPool;}
	
	public boolean isExpiry() {
		return false;
	}

	@Override
	public Object getParameter(String name) {
		return this.parameterPool.getParameter(name);
	}

	@Override
	public Object[] findParameters(String query) {
		return this.parameterPool.findParameters(query);
	}

	@Override
	public boolean setParameter(String name, Object value) {
		
		boolean result = this.parameterPool.setParameter(name, value);
		if(result && !this.parameterPool.isTransient(name))
		{
			//System.out.println("HERE======>DefaultAppSession.setParameter() name "+name+ "; THREAD: " +Thread.currentThread().getId());
			//System.out.println("HERE======>DefaultAppSession.setParameter() OS "+this.objectSegment+ "; THREAD: " +Thread.currentThread().getId());
			this.objectSegment.markDirty();
			//System.out.println("HERE======>DefaultAppSession.setParameter() name "+name+ "; THREAD: " +Thread.currentThread().getId());
		}
		return result;
	}
	
	@Override
	public String getApplicationId() {
		return this.objectSegment.getApplicationId();
	}

	@Override
	public String getSessionKey() {
		return this.sessionKey;
	}
	
	@Override
	public Date getUpdateTime() {
		return this.objectSegment.getLastUpdateTime();
	}

	@Override
	public void update() throws NoSuchAppSessionException {
		
		//System.out.println("HERE======>DefaultAppSession.update() #1 "+this.getSessionKey()+"; THREAD: "+Thread.currentThread().getId());		
		//System.out.println("HERE======>DefaultAppSession.update() #1 "+this+"; THREAD: "+Thread.currentThread().getId());
		this.checkForNewStatus(STATUS_UPDATED);
		
		//if no persistence service provider available,
		//then create a in-memory session (no persistent functionalities)
		if(!this.loadPersistentSP())
		{		
			//System.out.println("HERE======>DefaultAppSession.update() #1.1 "+this+"; THREAD: "+Thread.currentThread().getId());
			this.objectSegment = new AppSessionOS(
					this.factory.getApplicationId(),
					this.sessionKey, 
					new HashMap<String, Object>(), 
					new Date(System.currentTimeMillis()));
			
			this.status = STATUS_UPDATED;
			
			return;
		}
		
		//try to retrieve persistent states of this session
		AppSessionOS oldSession = this.spi.selectByKey(this.getFactory().getApplicationId(), this.sessionKey);
		//If the session has been destroyed by other thread
		if(STATUS_UPDATED == this.status //null!=this.objectSegment
				&& null==oldSession)
			throw new NoSuchAppSessionException("\'"+this.objectSegment.getSessionKey()+"\'");

		//if this session is the first time created.
		if(null == oldSession)
		{			
			//System.out.println("HERE======>DefaultAppSession.update() #1.2 "+this+"; THREAD: "+Thread.currentThread().getId());
			//Create a new session for the giving session key.
			this.objectSegment = new AppSessionOS(
					this.factory.getApplicationId(),
					this.sessionKey, 
					new HashMap<String, Object>(), 
					new Date(System.currentTimeMillis()));
			this.objectSegment.markNew();			
		}
		else
		{
			//System.out.println("HERE======>DefaultAppSession.update() #1.3 "+this+"; THREAD: "+Thread.currentThread().getId());
			//replace the current states
			this.objectSegment = oldSession;
			this.parameterPool.restore(this.objectSegment.getStates());	//restore parameters
		}
		//System.out.println("HERE======>DefaultAppSession.update() #DONE "+this+"; THREAD: "+Thread.currentThread().getId());
		this.status = STATUS_UPDATED;
	}

	@Override
	public void sync() throws InconsistentAppSessionException, NoSuchAppSessionException {
		//System.out.println("HERE======>DefaultAppSession.sync() #1 SESSION KEY: "+this.getSessionKey()+"; THREAD: "+Thread.currentThread().getId());
		
		this.checkForNewStatus(STATUS_RESET);
		
		//if no persistence service provider available.
		if(!this.loadPersistentSP()) return;
		
		//System.out.println("HERE======>DefaultAppSession.sync() IS NEW: "+this.objectSegment.isNew()+"; THREAD: "+Thread.currentThread().getId());
		//System.out.println("HERE======>DefaultAppSession.sync() IS DIRTY: "+this.objectSegment.isDirty()+"; THREAD: "+Thread.currentThread().getId());
		//System.out.println("HERE======>DefaultAppSession.sync() IS DELETED: "+this.objectSegment.isDeleted()+"; THREAD: "+Thread.currentThread().getId());
		
		//if there is no changes.
		if(this.objectSegment.isCleanMark()) return;
		
		boolean hasSession = this.spi.hasSession(this.getFactory().getApplicationId(), this.getSessionKey());
		//if this session has already created in other thread,
		//then need to call update() once, and try invoke this method again.
		if(this.objectSegment.isNew()
				&& hasSession)
			throw new InconsistentAppSessionException("Need to update session \'"+this.objectSegment.getSessionKey()+"\' before sync it.");
		
		//if this session has been destroyed by other thread
		if(this.objectSegment.isDirty()
				&& !hasSession)
			throw new NoSuchAppSessionException("Session \'"+this.objectSegment.getSessionKey()+"\' has been deleted (Invoking this method again will recreate this session).");
		
		//if this session has been destroyed.
		if(this.objectSegment.isDeleted()
				&& !hasSession) 
			return;
		
		//create this session's states
		if(this.objectSegment.isNew())
		{
			//System.out.println("HERE======>DefaultAppSession.sync() INSERT SESSION: "+this+"; THREAD: "+Thread.currentThread().getId());
			//insert
			Map<String, Object> states = this.parameterPool.getStates();
			this.objectSegment.setStates(states);	
			this.spi.insert(this.objectSegment); //insert
		}
		//update this session's states
		else if(this.objectSegment.isDirty())
		{			
			//System.out.println("HERE======>DefaultAppSession.sync() UPDATE SESSION: "+this+"; THREAD: "+Thread.currentThread().getId());
			Map<String, Object> states = this.parameterPool.getStates();
			this.objectSegment.setStates(states);	
			this.spi.update(this.objectSegment); //update			
		}
		//delete this session's states
		else if(this.objectSegment.isDeleted())
		{
			//System.out.println("HERE======>DefaultAppSession.sync() DELETE SESSION: "+this+"; THREAD: "+Thread.currentThread().getId());			
			this.spi.delete(this.objectSegment); //delete			
		}
		
		this.objectSegment.cleanMarks(); //clear the marks.
		
	}
	
	@Override
	public void close() {
		//System.out.println("HERE======>DefaultAppSession.close() "+this.objectSegment+"; THREAD: "+Thread.currentThread().getId());
		this.checkForNewStatus(STATUS_RESET);
		//if(!this.objectSegment.isCleanMark())
			//TODO: log message for warning there are some changes not been saved yet. 
		this.reset();
		this.factory.returnSession(this);
	}

	@Override
	public void destory() throws InconsistentAppSessionException, NoSuchAppSessionException {
		//System.out.println("HERE======>DefaultAppSession.destory() "+this.objectSegment+"; THREAD: "+Thread.currentThread().getId());
		
		this.checkForNewStatus(STATUS_DESTORYED);
		
		//mark this session need to be deleted
		this.objectSegment.markDeleted();
		this.sync(); //delete session's states via persistence service provider.
		this.factory.removeSession(this.getSessionKey()); //remove all sessions key from the pool
		this.parameterPool.clear();
		
	}
	
	@Override
	public AppSessionFactory getFactory() {
		return this.factory;
	}

	private boolean loadPersistentSP() {
		//try to retrieve persisted states to restore.
		if(null == this.spi)
		{		
			this.spi = (AppSessionSPI) this.getParameter(AppSessionSPI.class.getCanonicalName());
			//System.out.println("HERE======>DefaultAppSession.loadSPI() SPI "+this.spi+"; THREAD: "+Thread.currentThread().getId());
			if(null == this.spi)
			{
				//TODO: display warning level log message "Failed to load persistency layer SPI. This will cause session states cannot be propagated."
				return false;
			}			 
		}
		
		return true;
	}
	
	private void reset() {
		//System.out.println("HERE======>ABS APP SESSION _RESETTING "+this.objectSegment+"; THREAD: "+Thread.currentThread().getId());
		this.parameterPool.reset();
		if(null!=this.objectSegment) this.objectSegment.clean();
		this.status = STATUS_RESET;
	}

	private void checkForNewStatus(int newStatus) {
		if(STATUS_RESET == this.status)
		{
			if(STATUS_DESTORYED == newStatus)
				throw new IllegalStateException();
		}
		
		if(STATUS_DESTORYED == this.status)
		{
			if(STATUS_RESET == newStatus
					|| STATUS_UPDATED == newStatus
					|| STATUS_DESTORYED == newStatus)
				throw new IllegalStateException();
		}
	}
}
 