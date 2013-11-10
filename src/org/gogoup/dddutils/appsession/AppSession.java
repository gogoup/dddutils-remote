package org.gogoup.dddutils.appsession;

import java.util.Date;

public interface AppSession{
	
	public Object getParameter(String name);
	
	public boolean setParameter(String name, Object value);
	
	public Object[] findParameters(String query);
			
	public String getApplicationId();
	
	public String getSessionKey();
	
	public Date getUpdateTime();
	
	/**
	 * Updates the state of this session to what persisted with {@link AppSession#sync()} before.
	 * If there is no session exist, then a new session created.
	 * 
	 * @throws NoSuchAppSessionException When there is no persisted states can be retrieved
	 */
	public void update() throws NoSuchAppSessionException;
	
	/**
	 * Persist state of session.
	 * 
	 * @throws InconsistentAppSessionException TODO
	 * @throws NoSuchAppSessionException TODO
	 */
	public void sync() throws InconsistentAppSessionException, NoSuchAppSessionException;
	
	/**
	 * delete this session.
	 * @throws NoSuchAppSessionException 
	 * @throws InconsistentAppSessionException 
	 */
	public void destory() throws InconsistentAppSessionException, NoSuchAppSessionException;
	
	/**
	 * return this session back to session pool.
	 */
	public void close();
	
	public AppSessionFactory getFactory();
	
}
