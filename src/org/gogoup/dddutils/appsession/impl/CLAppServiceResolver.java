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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gogoup.dddutils.appsession.AppService;
import org.gogoup.dddutils.appsession.AppServiceResolver;

/**
 * This is a proxy class for delegating the application service resolvers loaded
 * via custom class loader {@link CLAppSession#loadClassByName(String)}.
 * 
 */
public class CLAppServiceResolver implements AppServiceResolver {
    
    private String[] serviceNames;
    private Map<String, AppServiceResolver> resolverDictionary;
    //private CLAppSession session;
    private String[] binaryClassNames;
    private ClassLoader classLoader;
    
    public CLAppServiceResolver(String[] clzNames, ClassLoader classLoader) {
        this.resolverDictionary = new HashMap<String, AppServiceResolver>();
        this.binaryClassNames = clzNames;
        this.serviceNames = null;
        this.classLoader = classLoader;
        
    }
    
    private AppServiceResolver loadAppServiceResolver(String binaryClassName) {
        
        AppServiceResolver resolver = null;
        try {
            Class<?> clz = this.classLoader.loadClass(binaryClassName);
            resolver = (AppServiceResolver) clz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        
        return resolver;
    }
    
    private AppServiceResolver fetchNotNullResolver(String name) {
        
        AppServiceResolver resolver = this.resolverDictionary.get(name);
        if (null == resolver)
            throw new NullPointerException(name);
        
        return resolver;
    }

    @Override
    public String[] getServiceNames() {
        
        if (null != this.serviceNames)
            return this.serviceNames;
        
        // loading parameter resolvers
        for (String clzName : this.binaryClassNames) {
            AppServiceResolver resolver = this.loadAppServiceResolver(clzName);
            String[] paramNames = resolver.getServiceNames();
            
            for (String pname : paramNames) {
                if (this.resolverDictionary.containsKey(pname)) {
                    throw new IllegalArgumentException("Resolver, " + resolver.getClass().getCanonicalName()
                            + "'s parameter name, \"" + pname + "\" has been registered! ");
                }
                this.resolverDictionary.put(pname, resolver);
            }
        }
        
        this.serviceNames = this.resolverDictionary.keySet().toArray(new String[this.resolverDictionary.size()]);
        
        return this.serviceNames;
    }
    
    @Override
    public AppService getAppService(String name) {
        
        return this.fetchNotNullResolver(name).getAppService(name);
    }
    
    @Override
    public String[] findAppServiceNames(String query) {
        
        Set<String> findNames = new LinkedHashSet<String>(this.serviceNames.length);
        for (Iterator<AppServiceResolver> iter = this.resolverDictionary.values().iterator(); iter.hasNext();) {
            String[] names = iter.next().findAppServiceNames(query);
            if (null == names)
                continue;
            for (String name : names)
                findNames.add(name);
        }
        
        return findNames.toArray(new String[findNames.size()]);
    }
    
    @Override
    public String[] getDependences(String name) {
        return this.fetchNotNullResolver(name).getDependences(name);
    }
    
    @Override
    public boolean verifyDependence(String name, AppService service) {
        return this.fetchNotNullResolver(name).verifyDependence(name, service);
    }
    
}
