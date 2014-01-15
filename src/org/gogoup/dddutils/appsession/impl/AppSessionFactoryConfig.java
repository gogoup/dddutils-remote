package org.gogoup.dddutils.appsession.impl;

import java.util.LinkedHashMap;
import java.util.Map;

public class AppSessionFactoryConfig {
    
    private String applicationId;
    private int concurrentSessionSize; // how many sessions for each session key.
    private int sessionCacheSize;
    private Map<String, Object> applicationParameters;
    
    public AppSessionFactoryConfig(String applicationId, int concurrentSessionSize,
            int sessionCacheSize) {
        this(applicationId, concurrentSessionSize, sessionCacheSize,
                new LinkedHashMap<String, Object>(0));
    }
    
    public AppSessionFactoryConfig(String applicationId, int concurrentSessionSize,
            int sessionCacheSize, Map<String, Object> applicationParameters) {
        
        this.applicationId = applicationId;        
        this.concurrentSessionSize = concurrentSessionSize;
        this.sessionCacheSize = sessionCacheSize;
        this.applicationParameters = applicationParameters;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public int getConcurrentSessionSize() {
        return concurrentSessionSize;
    }

    public int getSessionCacheSize() {
        return sessionCacheSize;
    }
    
    public Map<String, Object> getApplicationParameters() {
        return applicationParameters;
    }
}
