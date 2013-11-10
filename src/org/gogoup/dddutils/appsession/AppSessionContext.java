package org.gogoup.dddutils.appsession;

import org.gogoup.dddutils.pool.ParameterResolver;



public interface AppSessionContext{
		
	/**
	 * Register a ParameterResolver to the current session.
	 * 
	 * @param resolver
	 */
	public void registerParameterResolver(ParameterResolver resolver);
	
	/**
	 * Register a AppServiceResolver to the current session.
	 * 
	 * @param resolver
	 */
	public void registerAppServiceResolver(AppServiceResolver resolver);
	
	/**
	 * Returns the current session.
	 * 
	 * @return AppSession
	 */
	public AppSession getCurrentSession();
	
	public AppSession getSession(String key) throws NoSuchAppSessionException;
	
}
