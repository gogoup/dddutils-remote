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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gogoup.dddutils.appsession.AppService;
import org.gogoup.dddutils.appsession.AppServiceResolver;
import org.gogoup.dddutils.appsession.AppSession;
import org.gogoup.dddutils.appsession.AppSessionContext;
import org.gogoup.dddutils.appsession.AppSessionFactory;
import org.gogoup.dddutils.appsession.AppSessionFactoryDelegate;
import org.gogoup.dddutils.pool.ParameterPool;
import org.gogoup.dddutils.pool.resolver.TransientParameterResolver;

/**
 * 
 * According this resolver, application services {@link AppService} will be
 * dynamic deployed at runtime.
 * 
 * This resolver should be registered to each session only via session factory
 * delegate
 * {@link AppSessionFactoryDelegate#registerAppServiceResolvers(AppSessionFactory, AppSession)}
 * .
 * 
 * 
 */
public class CachedAppServiceResolver extends TransientParameterResolver implements AppServiceResolver {
    
    private AppSessionContext sessionContext;
    private AppServiceResolver helper;
    private Map<String, AppService> services;
    
    public CachedAppServiceResolver(AppSessionContext sessionContext, AppServiceResolver resolver) {
        super(resolver.getServiceNames());
        this.sessionContext = sessionContext;
        this.services = new LinkedHashMap<String, AppService>();
        this.helper = resolver;
    }
    
    protected AppSessionContext getAppSessionContext() {
        return sessionContext;
    }
    
    @Override
    public Object getParameter(ParameterPool pool, String name) {
        
        AppService service = this.services.get(name);
        if (null == service) {
            service = this.getAppService(name);
            if (null == service)
                return null;
            // notify for installation
            service.install(this.getAppSessionContext());
            this.services.put(name, service);
        }
        // notify for initialization
        service.init(this.getAppSessionContext());
        return service;
    }
    
    public String[] findParameterNames(ParameterPool pool, String query) {
        return this.findAppServiceNames(query);
    }
    
    @Override
    public boolean setParameter(ParameterPool pool, String name, Object value) {
        return false;
    }
    
    @Override
    public boolean isReadOnly(ParameterPool pool, String name) {
        return true;
    }
    
    @Override
    public void reset(ParameterPool pool) {
        for (Iterator<AppService> iter = this.services.values().iterator(); iter.hasNext();) {
            // notify for session closing.
            iter.next().close(this.getAppSessionContext());
        }
    }
    
    @Override
    public void clear(ParameterPool pool) {
        for (Iterator<AppService> iter = this.services.values().iterator(); iter.hasNext();) {
            // notify for session destory (uninstall)
            iter.next().uninstall(this.getAppSessionContext());
        }
        services.clear();
    }
    
    @Override
    public String[] getDependences(ParameterPool pool, String name) {
        return this.getDependences(name);
    }
    
    @Override
    public boolean verifyDependence(ParameterPool pool, String name, Object parameter) {
        return this.verifyDependence(name, (AppService) parameter);
    }

    @Override
    public String[] getServiceNames() {
        return helper.getServiceNames();
    }

    @Override
    public AppService getAppService(String name) {
        return helper.getAppService(name);
    }

    @Override
    public String[] findAppServiceNames(String query) {
        return helper.findAppServiceNames(query);
    }

    @Override
    public String[] getDependences(String name) {
        return helper.getDependences(name);
    }

    @Override
    public boolean verifyDependence(String name, AppService service) {
        return helper.verifyDependence(name, service);
    }
    
}
