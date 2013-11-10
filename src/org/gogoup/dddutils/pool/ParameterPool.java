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

import java.util.Map;

/**
 * ParameterPool is an aggregator class, in which all parameters must be accessed via 
 * associated ParameterResolvers.
 * 
 * @author ruisun
 *
 */
public interface ParameterPool {
		
	/**
	 * Return parameter value with the specified name. 
	 * 
	 * @param name String
	 * @return Object
	 */
	public Object getParameter(String name);
	
	/**
	 * set parameter value for the giving name.  
	 * 
	 * @param name String
	 * @param value Object
	 * @return boolean return false if the parameter value failed to set, otherwise, return true.
	 */
	public boolean setParameter(String name, Object value);
	
	/**
	 * Find parameter values which are satified to the query.
	 * 
	 * Different ParameterResolvers may have different way to process the query request.
	 * 
	 * @param query String
	 * @return Object[]
	 */
	public Object[] findParameters(String query);
	
	/**
	 * Register a parameter resolver to this pool.
	 * 
	 * Return a id which is associated with this ParameterResolver.
	 * 
	 * @param resolver ParameterResolver
	 * @return long
	 */
	public long registerParameterResolver(ParameterResolver resolver);
	
	/**
	 * Deregister a parameter resolver with the giving id.
	 * 
	 * @param id long
	 */
	public void deregisterParameterResolver(long id);
	
	/**
	 * Return all parameter names.
	 * 
	 * @return String[]
	 */
	public String[] getParameterNames();
	
	/**
	 * Retrieve persistentable parameter values. 
	 * 
	 * The key of the returned map is parameter name.
	 * 
	 * The item of the returned map is the persistentable values.
	 * 
	 * @return Map<String, Object> 
	 */
	public Map<String, Object> getStates();
	
	/**
	 * Restore persistented parameter values.
	 * 
	 * @param states
	 */
	public void restore(Map<String, Object> states);
	
	/**
	 * Reset the parameter values back to the initial state.
	 */
	public void reset();
	
	/**
	 * Clear all the parameter values, and deregister all parameter resolvers.
	 * 
	 */
	public void clear();
	
	/**
	 * Check if the parameter with the giving name is read-only.
	 * 
	 * @param name String
	 * @return boolean Return true if it is read-only, otherwirse, return false.
	 */
	public boolean isReadOnly(String name);
	
	/**
	 * Check if the parameter with the giving name need to be persisted.
	 * 
	 * @param name String
	 * @return boolean Return true if the parameter is not persistentable, otherwise return false.
	 */
	public boolean isTransient(String name);
}
