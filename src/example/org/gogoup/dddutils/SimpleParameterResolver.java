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

package example.org.gogoup.dddutils;


import org.gogoup.dddutils.pool.ParameterPool;
import org.gogoup.dddutils.pool.ParameterResolver;

public class SimpleParameterResolver implements ParameterResolver {
	
	private static final String PARAMETER1 = "PARAMETER1";

	private static final String[] names = new String[]{
		PARAMETER1
	};
	
	private String content;
	
	public SimpleParameterResolver() {

	}

	@Override
	public String[] getParameterNames(ParameterPool pool) {
		return names;
	}

	@Override
	public Object getParameter(ParameterPool pool, String name) {
		
		if(PARAMETER1.equals(name))
			return content;
		
		return null;
	}

	@Override
	public String[] findParameterNames(ParameterPool pool, String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setParameter(ParameterPool pool, String name,
			Object value) {
		
		if(PARAMETER1.equals(name))
		{
			content = (String) value;
			return true;
		}
		
		return false;
	}

	@Override
	public boolean isReadOnly(ParameterPool pool, String name) {
		
		return false;
	}

	@Override
	public boolean isTransient(ParameterPool pool, String name) {
		
		return false;
	}

	@Override
	public void reset(ParameterPool pool) {

	}

	@Override
	public void clear(ParameterPool pool) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getStates(ParameterPool pool, String name) {
		return this.getParameter(pool, name);
	}

	@Override
	public void restoreStates(ParameterPool pool, String name, Object state) {
		
		if(null == state) return;
		content = (String) state;
	}

	@Override
	public String[] getDependences(ParameterPool pool, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verifyDependence(ParameterPool pool, String name,
			Object parameter) {
		
		return false;
	}

}
