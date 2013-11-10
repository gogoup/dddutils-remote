package org.gogoup.dddutils.appsession;

import org.gogoup.dddutils.pool.ParameterResolver;


public interface AppSessionFactoryDelegate {
	
	public AppSession assembleAppSession(AppSessionFactory factory, String sessionKey);
	
	public ParameterResolver[] loadParameterResolvers(AppSessionFactory factory, AppSession session);
	
	public AppServiceResolver[] loadAppServiceResolvers(AppSessionFactory factory, AppSessionContext context);
}
