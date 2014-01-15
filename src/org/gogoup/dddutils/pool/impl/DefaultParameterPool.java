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

package org.gogoup.dddutils.pool.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gogoup.dddutils.pool.ParameterPool;
import org.gogoup.dddutils.pool.ParameterResolver;
import org.gogoup.dddutils.pool.resolver.SimpleParameterResolver;

public class DefaultParameterPool implements ParameterPool {
    
    private String[] parameterNames;
    private Map<String, Long> resolverIndexMap;
    private Map<Long, ParameterResolver> resolvers;
    private long resolverIndex;
    
    public DefaultParameterPool(ParameterResolver[] defaultResolvers) {
        this();
        this.parameterNames = null;
        for (ParameterResolver resolver : defaultResolvers)
            registerParameterResolver(resolver);
    }
    
    public DefaultParameterPool() {
        resolvers = new LinkedHashMap<Long, ParameterResolver>(20);
        resolverIndexMap = new LinkedHashMap<String, Long>(50);
        resolverIndex = 0;
    }
    
    @Override
    public boolean setParameter(String name, Object value) {
        
        if (!isParameterNameRegistered(name))
            registerParameterResolver(new SimpleParameterResolver(name));
        
        if (this.isReadOnly(name))
            throw new RuntimeException("Parameter \"" + name + "\"'s value is unmodifiable.");
        
        ParameterResolver resolver = this.resolvers.get(resolverIndexMap.get(name));
        return resolver.setParameter(this, name, value);
    }
    
    @Override
    public Object getParameter(String name) {
        if (!isParameterNameRegistered(name))
            return null;
        ParameterResolver resolver = this.resolvers.get(this.resolverIndexMap.get(name));
        return resolver.getParameter(this, name);
    }
    
    @Override
    public Object[] findParameters(String query) {
        
        // Use two seperate loops for resolver and parameters respectively
        // to present concurrent modification.
        
        List<String> foundParameterNames = new ArrayList<String>();
        for (Iterator<ParameterResolver> iter = this.resolvers.values().iterator(); iter.hasNext();) {
            ParameterResolver resolver = iter.next();
            
            String[] foundNames = resolver.findParameterNames(this, query);
            if (null == foundNames)
                continue;
            for (String name : foundNames) {
                foundParameterNames.add(name);
            }
        }
        
        List<Object> parameters = new ArrayList<Object>(foundParameterNames.size());
        for (int i = 0; i < foundParameterNames.size(); i++) {
            String name = foundParameterNames.get(i);
            parameters.add(this.getParameter(name));
        }
        
        return parameters.toArray(new Object[parameters.size()]);
    }
    
    @Override
    public long registerParameterResolver(ParameterResolver resolver) {
        // System.out.println("HERE======>DefaultParameterPool.registerParameterResolver() resolver: "+resolver);
        String[] names = resolver.getParameterNames(this);
        
        // System.out.println("HERE======>DefaultParameterPool.registerParameterResolver() resolverIndex: "+resolverIndex);
        this.resolvers.put(this.resolverIndex, resolver);
        
        for (int i = 0; i < names.length; i++) {
            String pname = names[i];
            if (isParameterNameRegistered(pname))
                throw new RuntimeException("Parameter name \"" + pname + "\" is already registered.");
            // verify dependent parameters
            String[] dependentParameterNames = resolver.getDependences(this, pname);
            if (null != dependentParameterNames) {
                for (String dname : dependentParameterNames) {
                    if (dname.equals(pname))
                        throw new IllegalArgumentException("Parameter name \"" + pname
                                + "\" is depending on self (This would cause a dead dependent loop).");
                    
                    if (!this.isParameterNameRegistered(dname))
                        throw new RuntimeException("Parameter name \"" + pname
                                + "\" failed to verify dependent parameter, " + dname);
                    Object param = this.getParameter(dname);
                    if (!resolver.verifyDependence(this, dname, param))
                        throw new RuntimeException("Parameter name \"" + pname
                                + "\" failed to verify dependent parameter, " + dname);
                }
            }
            
            this.resolverIndexMap.put(pname, this.resolverIndex);
        }
        
        this.parameterNames = null;
        long index = this.resolverIndex;
        this.resolverIndex++;
        return index;
    }
    
    @Override
    public void deregisterParameterResolver(long id) {
        // System.out.println("HERE======>DefaultParameterPool.deregisterParameterResolver() INDEX: "+index);
        ParameterResolver resolver = this.resolvers.remove(id);
        if (null == resolver)
            throw new NullPointerException();
        // System.out.println("HERE======>DefaultParameterPool.deregisterParameterResolver() resolver: "+resolver);
        String[] names = resolver.getParameterNames(this);
        for (int i = 0; i < names.length; i++) {
            String pname = names[i];
            // System.out.println("HERE======>DefaultParameterPool.deregisterParameterResolver() NAME: "+pname);
            this.resolverIndexMap.remove(pname);
        }
        resolver.clear(this);
        
    }
    
    @Override
    public String[] getParameterNames() {
        if (null == this.parameterNames)
            this.parameterNames = this.resolverIndexMap.keySet().toArray(new String[this.resolverIndexMap.size()]);
        
        return this.parameterNames;
    }
    
    private boolean isParameterNameRegistered(String name) {
        return resolverIndexMap.get(name) == null ? false : true;
    }
    
    @Override
    public Map<String, Object> getStates() {
        
        Map<String, Object> states = new HashMap<String, Object>();
        
        String[] names = this.getParameterNames();
        for (String name : names) {
            ParameterResolver resolver = this.resolvers.get(resolverIndexMap.get(name));
            if (!resolver.isTransient(this, name)) {
                states.put(name, resolver.getStates(this, name));
            }
        }
        
        return states;
    }
    
    @Override
    public void restore(Map<String, Object> states) {
        // System.out.println("HERE======>DefaultParameterPool.restore() STATES: "+states+"; THREAD: "+Thread.currentThread().getId());
        int restoreParameterCounter = 0;
        
        // only restore the parameters registered is keep the maximum backward
        // compatibilty
        String[] names = this.getParameterNames();
        for (String name : names) {
            // System.out.println("\nHERE======>DefaultParameterPool.restore() NAME: "+name+"; THREAD: "+Thread.currentThread().getId());
            // System.out.println("HERE======>DefaultParameterPool.restore() INDEX: "+this.resolverMap.get(name)+"; THREAD: "+Thread.currentThread().getId());
            ParameterResolver resolver = this.resolvers.get(this.resolverIndexMap.get(name));
            // System.out.println("HERE======>DefaultParameterPool.restore() RESOLVER: "+resolver+"; THREAD: "+Thread.currentThread().getId());
            // System.out.println("HERE======>DefaultParameterPool.restore() IS TRANSIENT: "+resolver.isTransient(name)+"; THREAD: "+Thread.currentThread().getId());
            if (resolver.isTransient(this, name) == false) {
                Object state = states.get(name);
                // System.out.println("HERE======>DefaultParameterPool.restore() STATE: "+state+"; THREAD: "+Thread.currentThread().getId());
                resolver.restoreStates(this, name, state);
                restoreParameterCounter++;
            }
        }
        
        try {
            if (restoreParameterCounter < states.size())
                throw new Exception("Unrestored states can be lost.");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    @Override
    public void reset() {
        // System.out.println("HERE======>DefaultParameterPool.reset() "+"; THREAD: "+Thread.currentThread().getId());
        for (Iterator<ParameterResolver> iter = this.resolvers.values().iterator(); iter.hasNext();) {
            ParameterResolver resolver = iter.next();
            // System.out.println("HERE======>DefaultParameterPool.reset() "+resolver+"; THREAD: "+Thread.currentThread().getId());
            resolver.reset(this);
        }
    }
    
    @Override
    public void clear() {
        
        for (Iterator<ParameterResolver> iter = this.resolvers.values().iterator(); iter.hasNext();) {
            ParameterResolver resolver = iter.next();
            resolver.clear(this);
        }
        // deregister all resolvers
        this.resolverIndexMap.clear();
        parameterNames = null;
    }
    
    @Override
    public boolean isReadOnly(String name) {
        
        if (!isParameterNameRegistered(name))
            throw new RuntimeException("Parameter name \"" + name + "\" does not exist.");
        ParameterResolver resolver = this.resolvers.get(resolverIndexMap.get(name));
        return resolver.isReadOnly(this, name);
    }
    
    @Override
    public boolean isTransient(String name) {
        
        if (!isParameterNameRegistered(name))
            throw new RuntimeException("Parameter name \"" + name + "\" does not exist.");
        ParameterResolver resolver = this.resolvers.get(resolverIndexMap.get(name));
        return resolver.isTransient(this, name);
    }
    
}