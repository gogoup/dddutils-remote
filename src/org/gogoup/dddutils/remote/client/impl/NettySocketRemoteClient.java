package org.gogoup.dddutils.remote.client.impl;

import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.gogoup.dddutils.remote.client.RemoteConnectionError;
import org.gogoup.dddutils.remote.client.RemoteConnectionException;
import org.gogoup.dddutils.remote.client.RemoteConnectionListener;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.ssl.SslHandler;

public class NettySocketRemoteClient extends AbstractNettyRemoteClient {

	private static final Logger logger = Logger.getLogger(NettyClientChannelFactory.class.getCanonicalName());
	
	public NettySocketRemoteClient(String host, int port, SSLContext sslContext) {
		super(host, port, sslContext);
	}

	public NettySocketRemoteClient(String host, int port, int initCapacity,
			int minCapacity, int maxCapacity, int increment,
			SSLContext sslContext) {
		super(host, port, initCapacity, minCapacity, maxCapacity, increment,
				sslContext);
	}

	@Override
	protected ChannelPipelineFactory assembblePipeLineFactory(
			SSLContext sslContext) {
		return new SocketClientPipelineFactory(sslContext);
	}

	@Override
	protected NettyRemoteConnection assembleConnection(
			AbstractNettyRemoteClient client, String host, int port,
			Channel channel) {
		return new NettySocketClientConnection(client, host, port, channel);
	}

	private static class SocketClientPipelineFactory implements ChannelPipelineFactory {
		
		private final SSLContext sslContext;
	
		public SocketClientPipelineFactory(SSLContext sslContext) {
			this.sslContext = sslContext;
		}
	
		public ChannelPipeline getPipeline() throws Exception {
		 	//Create a default pipeline implementation.
			ChannelPipeline pipeline = Channels.pipeline();            
	        //Enable HTTPS if necessary.
	        if (null != sslContext) 
	        {    			 
	        	//Uncomment the following line if you want HTTPS
	        	SSLEngine engine = sslContext.createSSLEngine();
	        	engine.setUseClientMode(true);
	        	SslHandler handler = new SslHandler(engine);
	        	handler.setIssueHandshake(false); //postpone issue handshake
	        	pipeline.addLast("ssl", new SslHandler(engine));            	
	        }	
	        
	        //this is used to handle the connection refused exception quietly.
	        pipeline.addLast("silencer", new SimpleChannelUpstreamHandler() {
			 @Override
			 public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
			 }
		 	});
	        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		    pipeline.addLast("decoder", new StringDecoder());
		    pipeline.addLast("encoder", new StringEncoder());		    		   
		    
		    return pipeline;
		}
	}

	private static class NettySocketClientConnection extends NettyRemoteConnection {
	
		private AbstractNettyRemoteClient apiClient;	
		private SocketResponseHandler handler;
		
		public NettySocketClientConnection(AbstractNettyRemoteClient apiClient, String host, int port, Channel channel) {
			super(apiClient, host, port, channel);
			
			this.apiClient=apiClient;		
			handler=new SocketResponseHandler(this);
			channel.getPipeline().addLast("handler", handler);		
		}
		
		@Override
		public void close() {
			this.apiClient.returnConnection(this);
		}
		
		@Override
		public boolean isClosed() {
			return !this.getChannel().isConnected();
		}
	
		@Override
		public Object sendRequest(Object data) {
			this.writeRequest((ChannelBuffer) data, null);
			handler.isWriteComplete();
			return handler.getResponseData();
		}
		
		/**
		 * Sends HTTP request synchronously.
		 * 
		 * @param data
		 * @param timeout
		 * @return
		 */
		@Override
	    public Object sendRequest(Object data, long timeout) throws RemoteConnectionException {
			this.writeRequest((ChannelBuffer) data, null);
			handler.isWriteComplete(timeout);
		    return handler.getResponseData(timeout);
	
		}
	    
	    @Override
		public void sendAsynchronousRequest(Object data, RemoteConnectionListener listener) {  
	    	throw new UnsupportedOperationException("NettyHttpClientConnection.sendAsynchronousRequest");
	    	//this.writeRequest((ChannelBuffer) data, listener);    		     	   
		}
	    
	    @Override
	    public void sendRequestAndForget(Object data) throws RemoteConnectionException{
	    	this.writeRequest((ChannelBuffer) data, null);
	    	logger.info("HERE======>sendRequestAndForget() #1"+ " THREAD: " +Thread.currentThread().getId());
	    	handler.isWriteComplete();
	    	logger.info("HERE======>sendRequestAndForget() #2"+ " THREAD: " +Thread.currentThread().getId());
	    }
	
		@Override
		public void sendRequestAndForget(Object data, long timeout)
				throws RemoteConnectionException {
			
			this.writeRequest((ChannelBuffer) data, null);
			logger.info("HERE======>sendRequestAndForget() #1"+ " THREAD: " +Thread.currentThread().getId());
	    	handler.isWriteComplete(timeout);
	    	logger.info("HERE======>sendRequestAndForget() #2"+ " THREAD: " +Thread.currentThread().getId());
		}
	
		private void writeRequest(ChannelBuffer buf, RemoteConnectionListener listener) {
	    	
	    	this.handler.reset();
	    	
	    	if(null != listener)
	    		handler.updateAPIClientConnectionListener(listener);
	    	
			this.getChannel().write(buf);
	    }
	
		private static class SocketResponseHandler extends SimpleChannelUpstreamHandler  {
	    			
			private RemoteConnectionListener listener;
			        
	        private boolean isReadComplete;
	        private boolean isWriteComplete;
	        private String responseData;
	        private NettyRemoteConnection connection;
	        
	    	public SocketResponseHandler(NettyRemoteConnection connection) { 
	    		this.reset();
	    		this.connection = connection;
	    	}
	    	
	    	public void reset() {
	    		this.responseData=null;
	    		this.isReadComplete=false;
	    		this.isWriteComplete=false;
	    	}
	    	
	    	public void updateAPIClientConnectionListener(RemoteConnectionListener listener) {
	    		this.listener=listener;
	    	}
	    	
	    	public boolean isWriteComplete() {
	    		
	    		synchronized(this) {											
																
		    		while(!isWriteComplete)
		    		{
		    			try {
		    				this.wait();	    					
						} catch (InterruptedException e1) {
							
						}
		    			
		    			//waked because of failed to get response data.(see exceptionCaught)
		    			if(!isWriteComplete) return false;
		    		}
		    		
		    		return true;
				}    		
	    	}
	    	
	    	public boolean isWriteComplete(long timeout) throws RemoteConnectionException {
	    		
	    		synchronized(this) {
	    			
					long startTime=timeout<0?0:System.currentTimeMillis();				
																
		    		while(!isWriteComplete)
		    		{
		    			try {
		    				this.wait(timeout);	    					
						} catch (InterruptedException e1) {
							
						}
		    			
		    			//exceeding the specified waiting time.
		    			if((System.currentTimeMillis()-startTime)>=timeout)
		    			{
		    				throw new RemoteConnectionException("", RemoteConnectionError.TIMEOUT);
		    			}
		    			
		    			//waked because of failed to get response data.(see exceptionCaught)
		    			if(!isWriteComplete) return false;	    				    		
		    		}
		    		
		    		return true;
				}    		
	    	}
	    	
	    	public String getResponseData() {
	    	
	    		synchronized(this) {											
																
		    		while(!isReadComplete)
		    		{
		    			try {
		    				this.wait();	    					
						} catch (InterruptedException e1) {
							
						}
		    			
		    			//waked because of failed to get response data.(see exceptionCaught)
		    			if(!isReadComplete) return null;
		    		}
		    		
		    		return this.responseData;
				}    		
	    	}
	    	
	    	public String getResponseData(long timeout) throws RemoteConnectionException {
	    		
	    		synchronized(this) {											
					long startTime=timeout<0?0:System.currentTimeMillis();				
																
		    		while(!isReadComplete)
		    		{
		    			try {
		    				
		    				this.wait(timeout);	    					
						} catch (InterruptedException e1) {
							
						}
		    		
		    			//exceeding the specified waiting time.
		    			if((System.currentTimeMillis()-startTime)>=timeout)
		    			{   	    		
		    				
		    				logger.warning("Failed to receive response data due to timeout");
		    				throw new RemoteConnectionException("", RemoteConnectionError.TIMEOUT);
		    			}
		    			
		    			//waked because of failed to get response data. (see exceptionCaught)
		    			if(!isReadComplete) return null;
		    			
		    			
		    		}
		    		
		    		return this.responseData;
				}    		
	    	}   
	    	
	    	private void finishReadingData(boolean isFinished) {
	    		synchronized(this) {
	    			this.isReadComplete=isFinished;
	    			this.notifyAll();
	    		}
	    	}
	    	
	    	private void finishWritingData(boolean isFinished) {
	    		synchronized(this) {
	    			this.isWriteComplete=isFinished;
	    			this.notifyAll();
	    		}
	    	}
	    	
	    	@Override
	    	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
	    		
	    		logger.warning("Channel "+e.getChannel()+" occures error due to "+e.getCause().getMessage());
	
	    		//Channel ch = e.getChannel();
	    		//if(ch.isOpen())
	    			//ch.close().awaitUninterruptibly(3000);
	    		
	    		//if(null!=listener) listener.readComplete(this.connection, false, null);//non-blocking; return null response data and failed
	    		
	    		//else finishReadingData(false); //blocking; waking the current waiting threads to return null resposne data.
	    		
	    		//to notify the listener if the request was sent asynchronously
	    		if(null!=listener)
	    		{
	    			listener.writeComplete(this.connection, false);
	    			listener.readComplete(this.connection, false, null);
	    		}
	    		//to notify the waiting threads if the request was sent asynchronously
	    		else
	    		{
	    			finishWritingData(false);
	    			finishReadingData(false);
	    		}
	    	}	    	
	    	
	    	@Override
	    	public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
	
	    		logger.info(this+" writeComplete(): CHANNEL: "+this.connection.getChannel()+ " THREAD: " +Thread.currentThread().getId());
	    		
	    		if(null!=listener) listener.writeComplete(this.connection, true); //non-blocking.
	    		else finishWritingData(true); //blocking
	    	}
	    	
	    	@Override
	    	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
	    		
	    		logger.info(this+" messageReceived(): CHANNEL: "+this.connection.getChannel()+ " THREAD: " +Thread.currentThread().getId());
	    	
	    		//no segment data need to take care for soket connection.
	    		this.responseData =(String) e.getMessage();
	    		logger.info("CONTENT {"+this.responseData+"}");
	    
	    		if(null!=listener) listener.readComplete(this.connection, true, this.responseData); //non-blocking.
				else finishReadingData(true); //blocking
	    			    		
	    	}
	    }
	}

}
