package org.gogoup.dddutils.remote.client.impl;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.gogoup.dddutils.remote.client.RemoteConnectionError;
import org.gogoup.dddutils.remote.client.RemoteConnectionException;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;

/**
 * This class is used to maintain the Netty channels.
 * 
 * NOTE: This implementation is not thread-safety.
 * 
 * 
 *
 */
public final class NettyClientChannelFactory {
	
	private static final Logger logger = Logger.getLogger(NettyClientChannelFactory.class.getCanonicalName());
	
	public final static int DEFAULT_INIT_CAPACITY = 1;
	public final static int DEFAULT_MAX_CAPACITY = 10;
	public final static int DEFAULT_MIN_CAPACITY = 1;
	public final static int DEFAULT_INCREMENT = 5;

	private String host;
	private int port;
	private ChannelFactory factory;
	private ClientBootstrap bootstrap;
	private ChannelPipelineFactory pipeLineFactory;
	private int initCapacity;
	private int minCapacity; //the minmium number of alive channels in the pool.
	private int maxCapacity;
	private int increment;
	ChannelGroup channelGroup = new DefaultChannelGroup("NettyClientChannelFactory");
	private final List<Channel> allChannels;
	private List<Channel> channelPool;
	private ChannelConnector channelConnector;
	
	public NettyClientChannelFactory(String host, int port, ChannelPipelineFactory pipeLineFactory) {
		this(host, port, DEFAULT_INIT_CAPACITY, DEFAULT_MIN_CAPACITY, DEFAULT_MAX_CAPACITY, DEFAULT_INCREMENT, pipeLineFactory);
    }
	
	public NettyClientChannelFactory(String host, int port, int initCapacity, int minCapacity, int maxCapacity, int increment,
			ChannelPipelineFactory pipeLineFactory) {
		
		if(maxCapacity == 0)
			throw new IllegalArgumentException("Max capacity need to be greater than ZERO.");
		
		if(initCapacity > maxCapacity)
			throw new IllegalArgumentException("Initial capacity cannot exceed max capacity, ("+initCapacity+" > "+maxCapacity+").");
		if(minCapacity > maxCapacity)
			throw new IllegalArgumentException("Min capacity cannot exceed max capacity, ("+initCapacity+" > "+maxCapacity+").");
		if(initCapacity < minCapacity)
			throw new IllegalArgumentException("Initial capacity cannot be less than min capacity, ("+initCapacity+" < "+minCapacity+").");
						
		logger.info("Remote channel admin \'"+host+":"+port+"\' will be intialized as " +
				"\nInitial Capacity: "+initCapacity+"" +
				"\nMinimum Capacity: "+minCapacity+"" +
				"\nMaximum Capacity: "+maxCapacity+"" +
				"\nIncrement: "+increment);
		
		this.initCapacity=initCapacity;
		this.minCapacity=minCapacity;
		this.maxCapacity=maxCapacity;
		this.increment=increment;
		this.allChannels=new LinkedList<Channel>();
		this.channelPool=new LinkedList<Channel>();
		this.host=host;
		this.port=port;
		channelConnector=new ChannelConnector();
	
		this.pipeLineFactory=pipeLineFactory;
		init();
		try {
			this.newChannels(this.initCapacity, 10000); //10 seconds
		} catch (RemoteConnectionException e) {
			logger.warning("Fail to initialize Netty channel pool for the giving capacity due to "+e.getError());
		}
    }
	
	public Channel getChannel() throws RemoteConnectionException {
		
		logger.info("The current number of channels in the pool: "+this.channelPool.size());
		
		if(this.channelPool.size()>0) return this.channelPool.remove(0);
		
		int num = this.calNumberOfNewChannels();
		
		logger.info(num+ " new channels need to be initialized.");
		
		if(0==num) return null;
		this.newChannels(num);		
		return this.channelPool.remove(0); 	    
	}
	
	public Channel getChannel(long timeout) throws RemoteConnectionException {
		
		logger.info("The current number of channels in the pool: "+this.channelPool.size()+" ("+timeout+")");
		
		if(this.channelPool.size()>0) return this.channelPool.remove(0);		
		
		int num = this.calNumberOfNewChannels();
		
		logger.info(num+ " new channels need to connect.");
		
		if(0==num) return null;		
		this.newChannels(num, timeout);		
		return this.channelPool.remove(0); 	    
	}
	
	/**
	 * Remove channel from pool and close it.
	 * 
	 * @param channel
	 */
	public void disposeChannel(Channel channel) {
		
		logger.warning("Channel "+channel+" need to be DISPOSED");
		this.allChannels.remove(channel);
		channel.close();
		logger.warning(this.channelPool.size() + " Channels remains in the pool");
	}

	public void close() {

		logger.info("Remote channel admin \'"+host+":"+port+"\' will be closed.");
		logger.info("Total "+this.allChannels+" connections ("+this.channelPool+" in the pool) need to be forced to close.");
				
		this.channelPool.clear();
		
		for (Iterator<Channel> iter = this.allChannels.iterator(); iter.hasNext();)
		{
			Channel ch =iter.next();
			if(ch.isConnected())
			{
				//ch.unbind().awaitUninterruptibly();
				ch.close().awaitUninterruptibly();
				ch.disconnect().awaitUninterruptibly();
				
			}
			this.channelGroup.remove(ch);
		}
		/**/
		this.allChannels.clear();		
		this.channelGroup.close().awaitUninterruptibly();		
		//Shut down executor threads to exit.
		this.bootstrap.releaseExternalResources();		
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getInitCapacity() {
		return initCapacity;
	}

	public int getMinCapacity() {
		return minCapacity;
	}

	public int getMaxCapacity() {
		return maxCapacity;
	}

	public int getIncrement() {
		return increment;
	}

	private void init() {
		
		//Configure the client connection factory.
 		this.factory = new OioClientSocketChannelFactory( //use Old IO client rather than New IO
 				Executors.newCachedThreadPool()); //worker count is based on the number of processor of in the running machine.
 		
 		bootstrap = new ClientBootstrap(factory); 		
 	    // Set up the event pipeline factory.
 	    bootstrap.setPipelineFactory(this.pipeLineFactory); 	    
 	    
 	    bootstrap.setOption("tcpNoDelay", true);
 	    //bootstrap.setOption("keepAlive", true); 	   
	}
	
	private int calNumberOfNewChannels() {
		
		if(this.channelPool.size() <= this.minCapacity
				&& this.allChannels.size() < this.maxCapacity)
		{						
			int size = this.increment;
			//after increase the number of channels, if the total number of channels is larger than the maximum
			//then adjusting the increment number.
			if((this.channelPool.size() + this.increment) > this.maxCapacity)
				size = this.maxCapacity - this.channelPool.size();
			
			return size;						
		}
		
		return 0;
	}

	private void newChannels(int num, long timeout) throws RemoteConnectionException {
		
		logger.info(num+" new channels need to connect. ("+timeout+")");
		
		for(int i=0; i<num; i++)
		{				 	   
			//long startTime=timeout<=0?0:System.currentTimeMillis();				
			Channel channel = this.channelConnector.connect(bootstrap, this.host, this.port, timeout);			
			
			if(RemoteConnectionError.NONE != this.channelConnector.getError())
				throw new RemoteConnectionException(this.channelConnector.getError() + " THREAD: " +Thread.currentThread().getId(), this.channelConnector.getError());
						 
	 	    this.allChannels.add(channel);
	 	    this.channelPool.add(channel);
	 	    channelGroup.add(channel);	
		}
		
	}
	
	private void newChannels(int num) throws RemoteConnectionException {
		
		logger.info(num+" new channels will be initialized.");
		
		for(int i=0; i<num; i++)
		{		
			Channel channel = this.channelConnector.connect(bootstrap, this.host, this.port);			
			
			if(RemoteConnectionError.NONE != this.channelConnector.getError())
				throw new RemoteConnectionException("", this.channelConnector.getError());
			
	 	    this.allChannels.add(channel);
	 	    this.channelPool.add(channel);
	 	    channelGroup.add(channel);	
		}		
			
	}
	
	private static class ChannelConnector{
		
		private Channel channel;
		private RemoteConnectionError error;		
		
		public ChannelConnector() {
			this.channel=null;
			this.error=RemoteConnectionError.NONE;			
		}		
		
		private void setChannel(Channel channel, RemoteConnectionError error) {
			synchronized(this) {
				
				this.channel=channel;
				this.error=error;				
				this.notifyAll(); //wake up all waiting threads on this object.				
			}
		}
		
		private RemoteConnectionError getError() {
			return this.error;
		}
		
		public Channel connect(ClientBootstrap bootstrap, String host, int port, long timeout) {
									
			synchronized(this) {				
							
				this.error=RemoteConnectionError.NONE;
				this.channel=null;
				long startTime=timeout<=0?0:System.currentTimeMillis();										
		 	    
				while(null==this.channel
						&& RemoteConnectionError.NONE == this.error)
	    		{									
			 	    
					ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
					
					logger.fine("Try to connect \'"+host+":"+port+"\' ("+timeout+")"+ " THREAD: " +Thread.currentThread().getId());
			 	    
			 	    future.addListener(new ChannelFutureListener() {
			 	    	 
						@Override
						public void operationComplete(ChannelFuture f) throws Exception {
							
							logger.fine("Begin listening to channel "+f.getChannel()+ " THREAD: " +Thread.currentThread().getId());
							
							/*
							 * return either success or failure channel.
							 */
							
							if (f.isCancelled()) 
							{
								logger.warning("Connection cancelled, "+f.getChannel()+ " THREAD: " +Thread.currentThread().getId());
								//f.getCause().printStackTrace();								
								setChannel(null, RemoteConnectionError.CONNECTION_FAILED); 
							}
							else if (!f.isSuccess()) 
							{
								logger.warning("Connection failed, "+f.getChannel()+ " THREAD: " +Thread.currentThread().getId());
								//f.getCause().printStackTrace();
								setChannel(null, RemoteConnectionError.CONNECTION_FAILED);
							} 
							else 
							{					
								logger.fine("Connected, "+f.getChannel()+ " THREAD: " +Thread.currentThread().getId());
						 	    						 	    
						 	    SslHandler sslHandler = f.getChannel().getPipeline().get(SslHandler.class);
								
						 	    if(null == sslHandler) 
						 	    	setChannel(f.getChannel(), RemoteConnectionError.NONE);
						 	    else
						 	    {
						 	    	//Begin handshake.		
									ChannelFuture handshakeFutrue=sslHandler.handshake(); 
									
									handshakeFutrue.addListener(new ChannelFutureListener() {
																
										@Override
										public void operationComplete(ChannelFuture future)
												throws Exception {
											
											logger.fine("#1 SUCCESS: " +future.isSuccess()+ " THREAD: " +Thread.currentThread().getId());
											
											if (future.isCancelled()) 
											{
												setChannel(null, RemoteConnectionError.CONNECTION_FAILED);	
											}
											else if(!future.isSuccess())
											{
												setChannel(null, RemoteConnectionError.HANDSHAKE_FAILED);
											}
											else
											{
												setChannel(future.getChannel(), RemoteConnectionError.NONE);
											}
											
											logger.fine("#2" + " THREAD: " +Thread.currentThread().getId());
											
										}
										
									});	
						 	    }
								
								
								logger.fine("Connected #2, "+f.getChannel()+ " THREAD: " +Thread.currentThread().getId());
							}
							
							logger.fine("Finish listening to channel "+f.getChannel()+ " THREAD: " +Thread.currentThread().getId());
										
						}
				 	});
			 	    
			 	    
			 	   if(null==this.channel) //in case of ChannelFuture return a channel right before get this point.
			 	   {
			 		  logger.info("Waiting for connecting to \'"+host+":"+port+"\' for "+timeout+"ms"+ " THREAD: " +Thread.currentThread().getId());
			 		  try {
			 			  this.wait(timeout);	    					
			 		  } catch (InterruptedException e1) {
			 			  e1.printStackTrace();
			 		  }
			 		  logger.info("Stop waiting for connecting to \'"+host+":"+port+"\'"+ " THREAD: " +Thread.currentThread().getId());
			 	   }			 	   
			 	   
			 	   if((System.currentTimeMillis()-startTime)>timeout)
			 	   {			 		   
			 		   logger.warning("Failed to connect to \'"+host+":"+port+"\' due to time out"+ " THREAD: " +Thread.currentThread().getId());
			 		   
			 		   if(null!=this.channel) this.channel.close();
			 		   setChannel(null, RemoteConnectionError.TIMEOUT);			 		   
			 	   }
	    			
	    		}
				
				logger.info("Channel "+this.channel+" has been initialized"+ " THREAD: " +Thread.currentThread().getId());

	    		return this.channel;
			}
		}
		
		public Channel connect(ClientBootstrap bootstrap, String host, int port) throws RemoteConnectionException {
			
			synchronized(this) {
				
				this.error=RemoteConnectionError.NONE;
				this.channel=null;				
				
				while(null==this.channel
						&& RemoteConnectionError.NONE == this.error)
	    		{				
					ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
					
					logger.fine("Start connecting to \'"+host+":"+port+"\'");
			 	   
			 	    future.addListener(new ChannelFutureListener() {
			 	    	 	    
						@Override
						public void operationComplete(ChannelFuture f) throws Exception {

							logger.fine("Begin listening to channel "+f.getChannel());
							
							if (f.isCancelled()) 
							{
								logger.warning("Connection cancelled, "+f.getChannel());
								//f.getCause().printStackTrace();
								setChannel(null, RemoteConnectionError.CONNECTION_FAILED);
							}
							else if (!f.isSuccess()) 
							{
								logger.warning("Connection failed, "+f.getChannel());
								//f.getCause().printStackTrace();
								setChannel(null, RemoteConnectionError.CONNECTION_FAILED);
							} 
							else 
							{			
								logger.fine("Connected, "+f.getChannel());

								SslHandler sslHandler = f.getChannel().getPipeline().get(SslHandler.class);
								
						 	    if(null == sslHandler)
						 	    	setChannel(f.getChannel(), RemoteConnectionError.NONE);
						 	    else
						 	    {
						 	    	// Begin handshake.		
									ChannelFuture handshakeFutrue=sslHandler.handshake(); 
									
									handshakeFutrue.addListener(new ChannelFutureListener() {
																
										@Override
										public void operationComplete(ChannelFuture future)
												throws Exception {
											
											logger.fine("#1 SUCCESS: " +future.isSuccess()+ " THREAD: " +Thread.currentThread().getId());
											
											if (future.isCancelled()) 
											{
												setChannel(null, RemoteConnectionError.CONNECTION_FAILED);	
											}
											else if(!future.isSuccess())
											{
												setChannel(null, RemoteConnectionError.HANDSHAKE_FAILED);
											}
											else
											{
												setChannel(future.getChannel(), RemoteConnectionError.NONE);
											}
											
											logger.fine("#2" + " THREAD: " +Thread.currentThread().getId());
											
										}
										
									});
						 	    }
						 	    
								
							}
							
							logger.fine("Finish listening to channel "+f.getChannel());	
						}
			 		   
			 	   });
			 	    
			 	   
			 	   if(null==this.channel)
			 	   {		
			 		  logger.fine("Waiting for connecting to \'"+host+":"+port+"\' forever");
			 		  try {
				 		   this.wait();	    					
				 	   } catch (InterruptedException e1) {
				 		   //e1.printStackTrace();
				 	   }
			 	   }
			 	   			 	  
	    		}
				
				logger.fine("Channel "+this.channel+" has been initialized");
	    		return this.channel;
			}
		}		
	 	
	}

}
