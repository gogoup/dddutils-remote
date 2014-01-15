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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.gogoup.dddutils.pool.ParameterPool;
import org.gogoup.dddutils.pool.ParameterResolver;

/**
 * This is a proxy class for delegating the parameter resolvers loaded via
 * custom class loader {@link CLAppSession#loadClassByName(String)}.
 * 
 * 
 */
public class CLParameterResolver implements ParameterResolver {
    
    private String[] parameterNames;
    private Map<String, ParameterResolver> resolverDictionary;
    //private CLAppSession session;
    private String[] classBinaryNames;
    private ClassLoader classLoader;
    
    public CLParameterResolver(String[] clzNames, ClassLoader classLoader) {
        
        //this.session = session;
        this.resolverDictionary = new HashMap<String, ParameterResolver>();
        this.classBinaryNames = clzNames;
        this.parameterNames = null;
        this.classLoader = classLoader;
        
    }
    
    private ParameterResolver loadParameterResolver(String binaryClassName) {
        
        ParameterResolver resolver = null;
        try {
            Class<?> clz = this.classLoader.loadClass(binaryClassName);
            resolver = (ParameterResolver) clz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        
        return resolver;
    }
    
    private ParameterResolver fetchNotNullResolver(String name) {
        
        ParameterResolver resolver = this.resolverDictionary.get(name);
        if (null == resolver)
            throw new NullPointerException(name);
        
        return resolver;
    }
    
    @Override
    public String[] getParameterNames(ParameterPool pool) {
        
        if (null != this.parameterNames)
            return this.parameterNames;
        
        // loading parameter resolvers
        for (String clzName : this.classBinaryNames) {
            ParameterResolver resolver = this.loadParameterResolver(clzName);
            String[] paramNames = resolver.getParameterNames(pool);
            
            for (String pname : paramNames) {
                if (this.resolverDictionary.containsKey(pname)) {
                    throw new IllegalArgumentException("Resolver, " + resolver.getClass().getCanonicalName()
                            + "'s parameter name, \"" + pname + "\" has been registered! ");
                }
                this.resolverDictionary.put(pname, resolver);
            }
        }
        
        this.parameterNames = this.resolverDictionary.keySet().toArray(new String[this.resolverDictionary.size()]);
        
        return this.parameterNames;
    }
    
    @Override
    public Object getParameter(ParameterPool pool, String name) {
        
        return this.fetchNotNullResolver(name).getParameter(pool, name);
    }
    
    public boolean setParameter(ParameterPool pool, String name, Object value) {
        return this.fetchNotNullResolver(name).setParameter(pool, name, value);
    }
    
    public boolean isReadOnly(ParameterPool pool, String name) {
        return this.fetchNotNullResolver(name).isReadOnly(pool, name);
    }
    
    public boolean isTransient(ParameterPool pool, String name) {
        return this.fetchNotNullResolver(name).isTransient(pool, name);
    }
    
    public void reset(ParameterPool pool) {
        
        for (Iterator<ParameterResolver> iter = this.resolverDictionary.values().iterator(); iter.hasNext();) {
            ParameterResolver resolver = iter.next();
            resolver.reset(pool);
        }
    }
    
    /**
     * Clear values
     */
    public void clear(ParameterPool pool) {
        
        for (Iterator<ParameterResolver> iter = this.resolverDictionary.values().iterator(); iter.hasNext();) {
            ParameterResolver resolver = iter.next();
            resolver.clear(pool);
        }
        
        this.resolverDictionary.clear();
        this.resolverDictionary = null;
        //this.session = null;
        this.classLoader = null;
    }
    
    public Object getStates(ParameterPool pool, String name) {
        return this.fetchNotNullResolver(name).getStates(pool, name);
    }
    
    public void restoreStates(ParameterPool pool, String name, Object state) {
        this.fetchNotNullResolver(name).restoreStates(pool, name, state);
    }
    
    public String[] getDependences(ParameterPool pool, String name) {
        return this.fetchNotNullResolver(name).getDependences(pool, name);
    }
    
    public boolean verifyDependence(ParameterPool pool, String name, Object parameter) {
        return this.fetchNotNullResolver(name).verifyDependence(pool, name, parameter);
    }
    
    @Override
    public String[] findParameterNames(ParameterPool pool, String query) {
        
        List<String> nameList = new LinkedList<String>();
        
        for (Iterator<ParameterResolver> iter = this.resolverDictionary.values().iterator(); iter.hasNext();) {
            ParameterResolver resolver = iter.next();
            String[] names = resolver.findParameterNames(pool, query);
            if (null == names)
                continue;
            for (String name : names) {
                nameList.add(name);
            }
        }
        
        return nameList.toArray(new String[nameList.size()]);
    }
    
}
