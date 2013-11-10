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

package org.gogoup.dddutils.remote.server;

public enum RemoteHttpMethod {
	OPTIONS("OPTIONS"),
	GET("GET"),
	HEAD("HEAD"),
	POST("POST"),
	PUT("PUT"),
	DELETE("DELETE"),
	TRACE("TRACE"),
	CONNECT("CONNECT");
	
	private String name;

	private RemoteHttpMethod(String name) {
		this.name=name;
	}
	
	public String getName() {
		return name;		
	}
	
	public static RemoteHttpMethod toRemoteHttpMethod(String name) {
		
		if(name.equalsIgnoreCase("OPTIONS"))
			return OPTIONS;
		else if(name.equalsIgnoreCase("GET"))
			return GET;
		else if(name.equalsIgnoreCase("HEAD"))
			return HEAD;
		else if(name.equalsIgnoreCase("POST"))
			return POST;
		else if(name.equalsIgnoreCase("PUT"))
			return PUT;
		else if(name.equalsIgnoreCase("DELETE"))
			return DELETE;
		else if(name.equalsIgnoreCase("TRACE"))
			return TRACE;
		else if(name.equalsIgnoreCase("CONNECT"))
			return CONNECT;
		
		return null;
	}
}
