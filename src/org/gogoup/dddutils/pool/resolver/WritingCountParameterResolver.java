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

package org.gogoup.dddutils.pool.resolver;

import java.util.HashMap;
import java.util.Map;

import org.gogoup.dddutils.pool.ParameterPool;

public class WritingCountParameterResolver extends TransientParameterResolver {
    
    public final static String MAX_ALLOWED_WRITE_PARAMETER_NAME_SUFFIX = "_MAX";
    public final static String WRITE_COUNTER_PARAMETER_NAME_SUFFIX = "_COUNTER";
    
    private Map<String, Object> parameterTable;
    private Map<String, Integer> maximumAllowedWriteTable;
    private Map<String, Integer> writeCounterTable;
    private String[] nameArray;
    
    public WritingCountParameterResolver(String[] names, int[] maximumAllowedWrite) {
        super(names);
        if (names.length != maximumAllowedWrite.length)
            throw new IllegalArgumentException("Inconsistent size of arguments.");
        
        parameterTable = new HashMap<String, Object>();
        maximumAllowedWriteTable = new HashMap<String, Integer>();
        writeCounterTable = new HashMap<String, Integer>();
        nameArray = new String[(names.length * 3)];
        
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            String maximumParamName = name + MAX_ALLOWED_WRITE_PARAMETER_NAME_SUFFIX;
            String counterParamName = name + WRITE_COUNTER_PARAMETER_NAME_SUFFIX;
            
            parameterTable.put(name, null);
            maximumAllowedWriteTable.put(maximumParamName, maximumAllowedWrite[i]);
            writeCounterTable.put(counterParamName, 0);
            
            int nameIndex = i * 3;
            nameArray[nameIndex] = name;
            nameArray[nameIndex + 1] = maximumParamName;
            nameArray[nameIndex + 2] = counterParamName;
        }
    }
    
    public WritingCountParameterResolver(String name, int maximumAllowedWrite) {
        this(new String[] { name }, new int[] { maximumAllowedWrite });
    }
    
    @Override
    public Object getParameter(ParameterPool pool, String name) {
        
        Object value = null;
        
        value = this.parameterTable.get(name);
        if (null != value)
            return value;
        
        value = this.maximumAllowedWriteTable.get(name);
        if (null != value)
            return value;
        
        value = this.writeCounterTable.get(name);
        if (null != value)
            return value;
        
        return value;
    }
    
    @Override
    public boolean setParameter(ParameterPool pool, String name, Object value) {
        
        if (null != this.parameterTable.get(name) && this.parameterTable.get(name).equals(value))
            return false;
        
        this.parameterTable.put(name, value);
        String counterParamName = name + WRITE_COUNTER_PARAMETER_NAME_SUFFIX;
        int counter = this.writeCounterTable.get(counterParamName);
        this.writeCounterTable.put(counterParamName, (counter + 1));
        
        return true;
    }
    
    @Override
    public String[] getParameterNames(ParameterPool pool) {
        return nameArray;
    }
    
    @Override
    public String[] findParameterNames(ParameterPool pool, String query) {
        return null;
    }
    
    @Override
    public boolean isReadOnly(ParameterPool pool, String name) {
        
        if (this.parameterTable.containsKey(name)) {
            int max = this.maximumAllowedWriteTable.get(name + MAX_ALLOWED_WRITE_PARAMETER_NAME_SUFFIX);
            int counter = this.writeCounterTable.get(name + WRITE_COUNTER_PARAMETER_NAME_SUFFIX);
            
            return (counter >= max);
        }
        
        return true;
    }
    
    @Override
    public void reset(ParameterPool pool) {
        
        // System.out.println("HERE======>WritingCountParameterResolver.reset() "+"; THREAD: "+Thread.currentThread().getId());
        for (String name : nameArray) {
            // System.out.println("HERE======>WRITING COUNT PARAM RESETTING..."+name+"; THREAD: "+Thread.currentThread().getId());
            if (this.parameterTable.containsKey(name))
                this.parameterTable.put(name, null);
            
            if (this.writeCounterTable.containsKey(name))
                this.writeCounterTable.put(name, 0);
        }
    }
    
    @Override
    public void clear(ParameterPool pool) {
        // System.out.println("HERE======>WritingCountParameterResolver.clear() "+"; THREAD: "+Thread.currentThread().getId());
        this.parameterTable.clear();
        this.maximumAllowedWriteTable.clear();
        this.writeCounterTable.clear();
        nameArray = null;
    }
    
    protected boolean hasParameterName(String name) {
        return this.parameterTable.containsKey(name);
    }
    
    protected Object getParameter(String name) {
        return parameterTable.get(name);
    }
    
    protected void setParameter(String name, Object parameter) {
        this.parameterTable.put(name, parameter);
    }
    
    protected Integer getMaxAllowedWrite(String name) {
        return this.maximumAllowedWriteTable.get(name);
    }
    
    protected void setMaxAllowedWrite(String name, Integer maximum) {
        this.maximumAllowedWriteTable.put(name, maximum);
    }
    
    protected Integer getWriteCounter(String name) {
        return this.writeCounterTable.get(name);
    }
    
    protected void setWriteCounter(String name, Integer counter) {
        this.writeCounterTable.put(name, counter);
    }
    
    @Override
    public String[] getDependences(ParameterPool pool, String name) {
        return null;
    }
    
    @Override
    public boolean verifyDependence(ParameterPool pool, String name, Object parameter) {
        return false;
    }
    
}
