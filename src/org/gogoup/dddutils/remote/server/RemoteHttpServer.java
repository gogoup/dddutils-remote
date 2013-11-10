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

import org.gogoup.dddutils.appsession.AppSessionFactory;

public abstract class RemoteHttpServer {

	private int port;
	private boolean sslEnabled;
	private AppSessionFactory sessionFactory;
	private long readerIdleTime;
	private long writerIdleTime;
	private long allIdleTime;
	private RemoteHttpServerDelegate delegate;
	
	public RemoteHttpServer(int port, boolean sslEnabled, AppSessionFactory sessionFactory, 
			long readerIdleTime, long writerIdleTime, long allIdleTime,
			RemoteHttpServerDelegate delegate) {
		
		this.port = port;
		this.sslEnabled = sslEnabled;
		this.sessionFactory = sessionFactory;
		this.readerIdleTime = readerIdleTime;
		this.writerIdleTime = writerIdleTime;
		this.allIdleTime = allIdleTime;
		this.delegate=delegate;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public boolean isSSLEnabled() {
		return this.sslEnabled;
	}
	
	public long getReaderIdleTime() {
		return readerIdleTime;
	}

	public long getWriterIdleTime() {
		return writerIdleTime;
	}

	public long getAllIdleTime() {
		return allIdleTime;
	}

	public AppSessionFactory getSessionFactory() {
		return this.sessionFactory;
	}
	
	public RemoteHttpServerDelegate getDelegate() {
		return delegate;
	}
	
	public abstract void startup();
	
	public abstract void shutdown();

}
