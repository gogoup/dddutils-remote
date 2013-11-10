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

package org.gogoup.dddutils.misc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.gogoup.dddutils.remote.client.RemoteClient;
import org.gogoup.dddutils.remote.client.RemoteConnection;
import org.gogoup.dddutils.remote.client.RemoteConnectionException;
import org.gogoup.dddutils.remote.client.impl.NettyHttpRemoteClient;

public final class HttpConnectionManager {

	private static HttpConnectionManager instance=null;
	private static final byte[] lock=new byte[0];
	private Map<String, RemoteClient> clientTable;
	
	private HttpConnectionManager() {
		clientTable=new HashMap<String, RemoteClient>();
	}
	
	public static HttpConnectionManager getInstance() {
		synchronized(lock){
			if(null==instance)
				instance=new HttpConnectionManager();//init
			return instance;
		}
	}
	
	public RemoteConnection getConnection(String url, long timeout) throws RemoteConnectionException {
		return this.getConnection(url, timeout, null);
	}
	
	public RemoteConnection getConnection(String url, long timeout, SSLContext sslContext) throws RemoteConnectionException {

		synchronized(lock){

			String urlString = url.toLowerCase();
			
			RemoteClient client=clientTable.get(urlString);
			if(null==client)
			{
	
				client=newClient(urlString, sslContext);
				clientTable.put(urlString, client);
			}
			RemoteConnection connection = null;
			connection = client.getConnection(timeout); //change the code in remote client to make this is elegent.
			
			return connection;
		}
	}
	
	private RemoteClient newClient(String urlString, SSLContext sslContext) {
		
		URI uri=null;

		try {
			
			uri=new URI(urlString);
			
		} catch (URISyntaxException e) {				
			e.printStackTrace();
		}

		String scheme = uri.getScheme() == null? "http" : uri.getScheme();
		String host = uri.getHost();
		
		int port;
		//uncomment this section for production.
		
		if (scheme.equalsIgnoreCase("http")) 
		{
			port = 80;
		} 
		else if (scheme.equalsIgnoreCase("https")) 
		{
			port = 443;
		}
		else		
		{
			throw new RuntimeException("Only HTTP(S) is supported.");		
		}
		
		RemoteClient client = new NettyHttpRemoteClient(host, 
				port, 1, 1, 20, 1, sslContext);

		return client;
	}
}
