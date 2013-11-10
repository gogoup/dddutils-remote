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

import org.gogoup.dddutils.pool.ParameterPool;
import org.gogoup.dddutils.pool.ParameterResolver;


public abstract class TransientParameterResolver implements ParameterResolver {

	private String[] names;
	
	public TransientParameterResolver(String[] names) {
		this.names = names;
	}
	
	@Override
	public String[] getParameterNames(ParameterPool pool) {return names;}
	
	@Override
	public boolean isTransient(ParameterPool pool, String name) {
		return true;
	}

	@Override
	public Object getStates(ParameterPool pool, String name) {
		throw new UnsupportedOperationException("due to parameter, "+name+", is transient.");
	}

	@Override
	public void restoreStates(ParameterPool pool, String name, Object state) {
		throw new UnsupportedOperationException("due to parameter, "+name+", is transient.");
	}

}
