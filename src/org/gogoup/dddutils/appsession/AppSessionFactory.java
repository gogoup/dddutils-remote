package org.gogoup.dddutils.appsession;

public interface AppSessionFactory {
	
	public String getApplicationId();
	
	public AppSession getSession(String key) throws NoSuchAppSessionException;
	
	public AppSessionFactoryDelegate getDelegate();
	
	public void setDelegate(AppSessionFactoryDelegate delegate);
}
