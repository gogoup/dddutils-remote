package org.gogoup.dddutils.remote.server.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gogoup.dddutils.appsession.AppServiceResolver;
import org.gogoup.dddutils.appsession.AppSession;
import org.gogoup.dddutils.appsession.AppSessionContext;
import org.gogoup.dddutils.appsession.AppSessionFactory;
import org.gogoup.dddutils.appsession.AppSessionFactoryDelegate;
import org.gogoup.dddutils.appsession.impl.DefaultAppSessionFactory;
import org.gogoup.dddutils.pool.ParameterResolver;
import org.gogoup.dddutils.remote.server.RemoteHttpServer;
import org.gogoup.dddutils.remote.server.RemoteHttpServerDelegate;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;


public class DefaultHttpServer extends RemoteHttpServer {
	
	private static final ChannelGroup allChannels = new DefaultChannelGroup("WebAPIServer");
		
	private ChannelFactory factory;
	private NettyHttpServerPipelineFactory piplelineFactory;
	private ExecutorService sessionExecutorPool;
	
	public DefaultHttpServer(int port, boolean sslEnabled,
			AppSessionFactory sessionFactory, long readerIdleTime,
			long writerIdleTime, long allIdleTime, ExecutorService sessionExecutorPool,
			RemoteHttpServerDelegate delegate) {
		
		super(port, sslEnabled, sessionFactory, readerIdleTime, 
				writerIdleTime, allIdleTime, delegate);
		
		AppSessionFactoryDelegate asfDelegate = new NettyAppSessionFactoryDelegate(
				sessionFactory.getDelegate());		
		sessionFactory.setDelegate(asfDelegate);
		
		this.piplelineFactory = new NettyHttpServerPipelineFactory(this);
		this.sessionExecutorPool=sessionExecutorPool;
	}
	
	public DefaultHttpServer(int port, boolean ssl, 
			DefaultAppSessionFactory sessionFactory, ExecutorService sessionExecutorPool,
			RemoteHttpServerDelegate delegate) {
    	this(port, ssl, sessionFactory, 15000, 30000, 15000, sessionExecutorPool, delegate);
    }
	
	public ExecutorService getSessionExecutorPool() {
		return this.sessionExecutorPool;
	}
	
	@Override
	public void startup() {
		// Configure the server.
    	factory =new NioServerSocketChannelFactory(
    			Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()
                );
    	
        ServerBootstrap bootstrap = new ServerBootstrap(factory);

        // Set up the event pipeline factory.            
        bootstrap.setPipelineFactory(this.piplelineFactory);
        
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        //bootstrap.setOption("child.reuseAddress", true);
        //bootstrap.setOption("child.connectTimeoutMillis", 10);

        // Bind and start to accept incoming connections.
        Channel channel=bootstrap.bind(new InetSocketAddress(this.getPort()));
        
        allChannels.add(channel);
	}

	@Override
	public void shutdown() {
		
        ChannelGroupFuture future = allChannels.close();
        future.awaitUninterruptibly();        
        factory.releaseExternalResources();
        System.out.println("Complete.");
        System.exit(0);
       
	}
	
	private static class NettyAppSessionFactoryDelegate implements AppSessionFactoryDelegate {
		
		private AppSessionFactoryDelegate delegate;
		
		public NettyAppSessionFactoryDelegate(AppSessionFactoryDelegate delegate) {
			this.delegate=delegate;
		}

		@Override
		public AppSession assembleAppSession(AppSessionFactory factory,
				String sessionKey) {
			
			return this.delegate.assembleAppSession(factory, sessionKey);
		}

		@Override
		public ParameterResolver[] loadParameterResolvers(AppSessionFactory factory, AppSession session) {
			
			ParameterResolver[] oldResolvers = this.delegate.loadParameterResolvers(factory, session);
			ParameterResolver[] newResolvers = new ParameterResolver[oldResolvers.length + 1];
			newResolvers[0] = new NettyHttpInfoResolver();
			System.arraycopy(oldResolvers, 0, newResolvers, 1, oldResolvers.length);
			
			return newResolvers;
		}

		@Override
		public AppServiceResolver[] loadAppServiceResolvers(AppSessionFactory factory, AppSessionContext context) {
			return this.delegate.loadAppServiceResolvers(factory, context);
		}		
	}
	
	
}
