package org.gogoup.dddutils.appsession.impl;

import org.gogoup.dddutils.appsession.AppService;
import org.gogoup.dddutils.appsession.AppSessionContext;

public abstract class AbstractAppService implements AppService {
    
    public AppSessionContext context;
    
    public AbstractAppService() { }
    
    protected AppSessionContext getAppSessionContext() { 
        return context;
    }

    @Override
    public void install(AppSessionContext context) {
        this.context = context;
    }

    @Override
    public void init(AppSessionContext context) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void close(AppSessionContext context) {

    }

    @Override
    public void uninstall(AppSessionContext context) {
        this.context = null;
    }
    
}
