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

package org.gogoup.dddutils.appsession;

import org.gogoup.dddutils.pool.ParameterResolver;



public interface AppSessionContext{
		
	/**
	 * Register a ParameterResolver to the current session.
	 * 
	 * @param resolver
	 */
	public void registerParameterResolver(ParameterResolver resolver);
	
	/**
	 * Register a AppServiceResolver to the current session.
	 * 
	 * @param resolver
	 */
	public void registerAppServiceResolver(AppServiceResolver resolver);
	
	/**
	 * Returns the current session.
	 * 
	 * @return AppSession
	 */
	public AppSession getCurrentSession();
	
	public AppSession getSession(String key) throws NoSuchAppSessionException;
	
}
