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

package org.gogoup.dddutils.pool;

public interface ParameterResolver {
    
    public String[] getParameterNames(ParameterPool pool);
    
    public Object getParameter(ParameterPool pool, String name);
    
    /**
     * Returns parameter names which are meet the specified query.
     * 
     * @param pool
     *            ParameterPool
     * @param query
     *            String
     * @return String[] - Return null value is this resolver does not support
     *         {@link ParameterPool#findParameters(String)}
     */
    public String[] findParameterNames(ParameterPool pool, String query);
    
    /**
     * Updates specified parameter with the giving value.
     * 
     * @param pool
     *            TODO
     * @param name
     * @param value
     * @return if the value updates the existing one, then return true,
     *         otherwise false.
     */
    public boolean setParameter(ParameterPool pool, String name, Object value);
    
    public boolean isReadOnly(ParameterPool pool, String name);
    
    /**
     * Check if the specified parameter is transient.
     * 
     * If it is, then the methods {{@link #getState(String)} and {
     * {@link #restore(String, Object)} will not supported by the resolver.
     * 
     * @param pool
     *            TODO
     * @param name
     * 
     * @return
     */
    public boolean isTransient(ParameterPool pool, String name);
    
    /**
     * Reset values
     */
    public void reset(ParameterPool pool);
    
    /**
     * Clear values
     */
    public void clear(ParameterPool pool);
    
    /**
     * Retrieve persistentable value for the specified parameter name.
     * 
     * @param pool
     * @param name
     * @return
     */
    public Object getStates(ParameterPool pool, String name);
    
    /**
     * Restore persistented value for the specified parameter name.
     * 
     * @param pool
     * @param name
     * @param state
     */
    public void restoreStates(ParameterPool pool, String name, Object state);
    
    public String[] getDependences(ParameterPool pool, String name);
    
    public boolean verifyDependence(ParameterPool pool, String name, Object parameter);
    /*
     * public boolean isDirty();
     */
    
}
