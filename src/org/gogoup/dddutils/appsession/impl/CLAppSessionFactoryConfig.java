package org.gogoup.dddutils.appsession.impl;

import java.util.HashMap;
import java.util.Map;

public class CLAppSessionFactoryConfig extends AppSessionFactoryConfig {
    
    private String applicationBase;
    private String[] locations;
    private String[] parameterResolvers;
    private String[] appServiceResolvers;
    private String[] applicationParameterResolvers;
    
    public CLAppSessionFactoryConfig(String applicationId, String applicationBase, 
            int concurrentSessionSize, int sessionCacheSize, Map<String, Object> parameters,
            String[] locations, String[] parameterResolvers, String[] appServiceResolvers,
            String[] applicationParameterResolvers) {
        super(applicationId, concurrentSessionSize, sessionCacheSize, parameters);     

        this.applicationBase = applicationBase;
        this.locations = removeDuplicates(locations);
        this.parameterResolvers = removeDuplicates(parameterResolvers);
        this.appServiceResolvers = removeDuplicates(appServiceResolvers);
        this.applicationParameterResolvers = removeDuplicates(applicationParameterResolvers);
    }
    public String getApplicationBase() {
        return applicationBase;
    }
    
    public String[] getLocations() {
        return locations;
    }

    public String[] getParameterResolvers() {
        return parameterResolvers;
    }

    public String[] getAppServiceResolvers() {
        return appServiceResolvers;
    }

    public String[] getApplicationParameterResolvers() {
        return applicationParameterResolvers;
    }
    private static String[] removeDuplicates(String[] items) {
        
        Map<String, String> filteredItems = new HashMap<String, String>();
        for(String item: items) {
            String key = item.toLowerCase();
            if (!filteredItems.containsKey(key)) {
                filteredItems.put(key, item);
            }
        }
        
        return filteredItems.values().toArray(new String[filteredItems.size()]);
    }
}
