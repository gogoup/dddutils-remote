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
