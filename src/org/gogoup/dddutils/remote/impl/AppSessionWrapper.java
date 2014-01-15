package org.gogoup.dddutils.remote.impl;

import java.util.Date;

import org.gogoup.dddutils.appsession.AppSession;
import org.gogoup.dddutils.appsession.AppSessionContext;
import org.gogoup.dddutils.appsession.InconsistentAppSessionException;
import org.gogoup.dddutils.appsession.NoSuchAppSessionException;

public class AppSessionWrapper implements AppSession {
    
    private static final int STATUS_NORMAL = 0;
    private static final int STATUS_CLOSED = 1;
    private static final int STATUS_DESTROYED = 2;
    
    private int status;
    private AppSession appSession;
    
    public AppSessionWrapper(AppSession appSession) {
        this.status = STATUS_NORMAL;
        this.appSession = appSession;
    }
    
    public AppSession getAppSession() {
        return appSession;
    }
    
    @Override
    public Object getParameter(String name) {
        checkForStatus();
        return appSession.getParameter(name);
    }
    
    @Override
    public boolean setParameter(String name, Object value) {
        checkForStatus();
        return appSession.setParameter(name, value);
    }
    
    @Override
    public Object[] findParameters(String query) {
        checkForStatus();
        return appSession.findParameters(query);
    }
    
    @Override
    public String getApplicationId() {
        checkForStatus();
        return appSession.getApplicationId();
    }
    
    @Override
    public String getSessionKey() {
        checkForStatus();
        return appSession.getSessionKey();
    }
    
    @Override
    public Date getUpdateTime() {
        checkForStatus();
        return appSession.getUpdateTime();
    }
    
    @Override
    public void update() throws NoSuchAppSessionException {
        checkForStatus();
        appSession.update();
    }
    
    @Override
    public void sync() throws InconsistentAppSessionException, NoSuchAppSessionException {
        checkForStatus();
        appSession.sync();
    }
    
    @Override
    public void destory() throws InconsistentAppSessionException, NoSuchAppSessionException {
        checkForStatus();
        appSession.destory();
        appSession = null;
        this.status = STATUS_DESTROYED;
    }
    
    @Override
    public void close() {
        if (STATUS_NORMAL != this.status) {
            return;
        }
        appSession.close();
        appSession = null;
        this.status = STATUS_CLOSED;
    }
    
    @Override
    public AppSessionContext getAppSessionContext() {
        checkForStatus();
        return appSession.getAppSessionContext();
    }
    
    private void checkForStatus() {
        if (STATUS_CLOSED == this.status) {
            throw new IllegalStateException("Session has been closed.");
        }
        if (STATUS_DESTROYED == this.status) {
            throw new IllegalStateException("Session has been destroyed.");
        } 
    }
    
}
