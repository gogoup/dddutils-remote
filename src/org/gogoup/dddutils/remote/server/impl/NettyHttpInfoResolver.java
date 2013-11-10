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

package org.gogoup.dddutils.remote.server.impl;

import java.util.Map;
import java.util.Set;

import org.gogoup.dddutils.pool.ParameterPool;
import org.gogoup.dddutils.pool.resolver.TransientParameterResolver;
import org.gogoup.dddutils.remote.server.SessionParameterNames;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * This class is a implementation of ParameterResolver interface.
 * 
 * This class also provides an adaptor function to register other http info 
 * resolvers (http header and url parameters) as a http request recieved 
 * and deregister these resolvers as the process of the http request finished. 
 * 
 * @author ruisun
 *
 */
public class NettyHttpInfoResolver extends TransientParameterResolver {

	private String[] headerNames;
	private String[] parameterNames;	
	private long headerResolverIndex;
	private long parameterResolverIndex;
	private String urlPath;
	private ChannelBuffer requestData;
	
	public NettyHttpInfoResolver() {
		super(new String[]{
				SessionParameterNames.HTTP_HEADER_NAMES,
				SessionParameterNames.HTTP_HEADER,
				SessionParameterNames.URL_PARAMETER_NAMES,
				SessionParameterNames.URL_PARAMETER,
				SessionParameterNames.URL_PATH,
				SessionParameterNames.HTTP_REQUEST_DATA
		});
		
		this.headerResolverIndex = -1;
		this.parameterResolverIndex = -1;
		this.urlPath=null;
		this.requestData=null;
		
	}

	@Override
	public Object getParameter(ParameterPool pool, String name) {

		if(SessionParameterNames.HTTP_HEADER_NAMES.equals(name))
			return this.headerNames;
		if(SessionParameterNames.URL_PARAMETER_NAMES.equals(name))
			return this.parameterNames;
		
		if(SessionParameterNames.HTTP_HEADER.equals(name))
			throw new UnsupportedOperationException("Get parameter, "+name);
		if(SessionParameterNames.URL_PARAMETER.equals(name))
			throw new UnsupportedOperationException("Get parameter, "+name);
		
		if(SessionParameterNames.URL_PATH.equals(name))
			return this.urlPath;
		if(SessionParameterNames.HTTP_REQUEST_DATA.equals(name))
			return this.requestData.toByteBuffer();
		
		return null;
	}

	@Override
	public String[] findParameterNames(ParameterPool pool, String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean setParameter(ParameterPool pool, String name, Object value) {
		
		if(SessionParameterNames.HTTP_HEADER.equals(name))
		{
			if(-1 != this.headerResolverIndex)
			{
				pool.deregisterParameterResolver(this.headerResolverIndex);
			}
	
			Map<String, String[]> headers = (Map<String, String[]>) value;
			Set<String> nameSet = headers.keySet();
			this.headerNames = nameSet.toArray(new String[nameSet.size()]);
			this.headerResolverIndex = pool.registerParameterResolver(new InfoResolver(this.headerNames, headers));
			
			return true;
		}
			
		if(SessionParameterNames.URL_PARAMETER.equals(name))
		{
			if(-1 != this.parameterResolverIndex)
			{
				pool.deregisterParameterResolver(this.parameterResolverIndex);
			}
		
			Map<String, String[]> parameters = (Map<String, String[]>) value;
			Set<String> nameSet = parameters.keySet();
			this.parameterNames = nameSet.toArray(new String[nameSet.size()]);	
			this.parameterResolverIndex = pool.registerParameterResolver(new InfoResolver(this.parameterNames, parameters));
			
			return true;
		}
		
		if(SessionParameterNames.URL_PATH.equals(name))
		{
			this.urlPath=(String) value;
		}
		
		if(SessionParameterNames.HTTP_REQUEST_DATA.equals(name))
		{
			this.requestData = (ChannelBuffer) value;
		}
		
		return false;
	}

	@Override
	public boolean isReadOnly(ParameterPool pool, String name) {

		if(SessionParameterNames.HTTP_HEADER.equals(name))
			return false;
		if(SessionParameterNames.URL_PARAMETER.equals(name))
			return false;
		if(SessionParameterNames.URL_PATH.equals(name)
				&& this.urlPath == null)
			return false;
		if(SessionParameterNames.HTTP_REQUEST_DATA.equals(name)
				&& this.requestData == null)
			return false;
		
		return true;
	}

	@Override
	public void reset(ParameterPool pool) {
		this.urlPath = null;
		this.requestData = null;
	}

	@Override
	public void clear(ParameterPool pool) {
		
		this.headerNames = null;
		this.parameterNames = null;
		this.headerResolverIndex = -1;
		this.parameterResolverIndex = -1;
		this.reset(pool);
	}

	@Override
	public String[] getDependences(ParameterPool pool, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verifyDependence(ParameterPool pool, String name,
			Object parameter) {
		// TODO Auto-generated method stub
		return false;
	}

	public class InfoResolver extends TransientParameterResolver {
		
		private Map<String, String[]> info;
			
		public InfoResolver(String[] names, Map<String, String[]> info) {
			super(names);							
			this.info=info;		
		}	
	
		@Override
		public Object getParameter(ParameterPool pool, String name) {
			return this.info.get(name);
		}

		@Override
		public String[] findParameterNames(ParameterPool pool, String query) {
			// TODO Auto-generated method stub
			return null;
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
		}

		@Override
		public void clear(ParameterPool pool) {
			info.clear();
		}

		@Override
		public String[] getDependences(ParameterPool pool, String name) {		
			return null;
		}

		@Override
		public boolean verifyDependence(ParameterPool pool, String name,
				Object parameter) {
			return false;
		}

	}
}
