package org.gogoup.dddutils.appsession;

public interface AppServiceResolver {
    
    public String[] getServiceNames();
    
    public AppService getAppService(String name);
    
    public String[] findAppServiceNames(String query);
    
    public String[] getDependences(String name);
    
    public boolean verifyDependence(String name, AppService service);
    
}
