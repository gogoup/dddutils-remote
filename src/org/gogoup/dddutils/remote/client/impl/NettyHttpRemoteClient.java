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

package org.gogoup.dddutils.remote.client.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.gogoup.dddutils.remote.client.RemoteConnectionError;
import org.gogoup.dddutils.remote.client.RemoteConnectionException;
import org.gogoup.dddutils.remote.client.RemoteConnectionListener;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.ssl.SslHandler;

public class NettyHttpRemoteClient extends AbstractNettyRemoteClient {

	private static final Logger logger = Logger.getLogger(NettyHttpRemoteClient.class.getCanonicalName());
	
	public NettyHttpRemoteClient(String host, int port, SSLContext sslContext) {
		super(host, port, sslContext);
	
	}
	public NettyHttpRemoteClient(String host, int port, int initCapacity,
			int minCapacity, int maxCapacity, int increment,
			SSLContext sslContext) {
		super(host, port, initCapacity, minCapacity, maxCapacity, increment, sslContext);
	}

	@Override
	protected ChannelPipelineFactory assembblePipeLineFactory(
			SSLContext sslContext) {
		return new HttpClientPipelineFactory(sslContext);
	}
	@Override
	protected NettyRemoteConnection assembleConnection(
			AbstractNettyRemoteClient client, String host, int port,
			Channel channel) {
		
		return new NettyHttpClientConnection(client, host, port, channel);
	}

	static class HttpClientPipelineFactory implements ChannelPipelineFactory {
		    	
		 private final SSLContext sslContext;
		          
		 public HttpClientPipelineFactory(SSLContext sslContext) {
			 this.sslContext = sslContext;    		
		 }
	
		 public ChannelPipeline getPipeline() throws Exception {
			 //Create a default pipeline implementation.
	         ChannelPipeline pipeline = Channels.pipeline();
	         
			 //Enable HTTPS if necessary.
			 if (null != sslContext) {    			 
				 //Uncomment the following line if you want HTTPS
				 SSLEngine engine = sslContext.createSSLEngine();
				 engine.setUseClientMode(true);
				 SslHandler handler = new SslHandler(engine);
				 handler.setIssueHandshake(false); //postpone issue handshake			
				 pipeline.addLast("ssl", handler);
			 }	
			     		 
			 //this is used to handle the connection refused exception quietly.
			 /*pipeline.addLast("silencer", new SimpleChannelUpstreamHandler() {
				 @Override
				 public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {    				
				 }
			 });*/
			 pipeline.addLast("decoder", new HttpResponseDecoder());       		 
			 //Uncomment the following line if you don't want to handle HttpChunks.
			 //pipeline.addLast("aggregator", new HttpChunkAggregator(10485760)); //10MB 
			 pipeline.addLast("encoder", new HttpRequestEncoder());     		 
			 //Remove the following line if you don't want automatic content compression.
			 pipeline.addLast("inflater", new HttpContentDecompressor());
			
			 return pipeline;
		 }
	}

	private static class NettyHttpClientConnection extends NettyRemoteConnection{
	
		private AbstractNettyRemoteClient client;
		//private URI uri;
		//private Channel channel;
		private HttpResponseHandler handler;
		
		public NettyHttpClientConnection(AbstractNettyRemoteClient client, String host, int port, Channel channel) {
			super(client, host, port, channel);
			
			this.client=client;
			//append handler
			handler=new HttpResponseHandler(this);
			channel.getPipeline().addLast("handler", handler);			
		}
		
		@Override
		public void close() {
			logger.info("Close Connection "+this.getChannel()+"."+ " THREAD: " +Thread.currentThread().getId());
			this.client.returnConnection(this);			
		}
		
		@Override
		public boolean isClosed() {
			if(!this.getChannel().isOpen()
					|| !this.getChannel().isConnected())
				return true;
			else
				return false;
		}
	
		@Override
		public Object sendRequest(Object data) {
			this.writeRequest((HttpRequest) data, null); //fire the request first
			if(!handler.isWriteComplete())
			{
				logger.warning("Send Request Failed!"+ " THREAD: " +Thread.currentThread().getId());
				return null;
			}
			
			return handler.getResponseData();
		}
		
		/**
		 * Sends HTTP request synchronously.
		 * 
		 * @param data
		 * @param timeout
		 * @return
		 * @throws RemoteConnectionException 
		 */
		@Override
	    public Object sendRequest(Object data, long timeout) throws RemoteConnectionException {
			
			this.writeRequest((HttpRequest) data, null);
			if(!handler.isWriteComplete(timeout))
			{
				logger.warning("Send Request Failed!"+ " THREAD: " +Thread.currentThread().getId());
				return null;
			}
	
			return handler.getResponseData(timeout);	
			
		}
	    
	    @Override
		public void sendAsynchronousRequest(Object data, RemoteConnectionListener listener) {
	    	//throw new UnsupportedOperationException("NettyHttpClientConnection.sendAsynchronousRequest");
	    	this.writeRequest((HttpRequest) data, listener);
	    	
		} 
	    
	    @Override
	    public void sendRequestAndForget(Object data) {
	    	this.writeRequest((HttpRequest) data, null);
	    	logger.info("HERE======>sendRequestAndForget() #1"+ " THREAD: " +Thread.currentThread().getId());
	    	handler.isWriteComplete();
	    	logger.info("HERE======>sendRequestAndForget() #2"+ " THREAD: " +Thread.currentThread().getId());
	    }
	
		@Override
		public void sendRequestAndForget(Object data, long timeout) throws RemoteConnectionException {
			this.writeRequest((HttpRequest) data, null);
			logger.info("HERE======>sendRequestAndForget() #1"+ " THREAD: " +Thread.currentThread().getId());
	    	handler.isWriteComplete(timeout);
	    	logger.info("HERE======>sendRequestAndForget() #2"+ " THREAD: " +Thread.currentThread().getId());
		}
	
		private void writeRequest(HttpRequest request, RemoteConnectionListener listener) { 
			
	    	this.handler.reset();
			
	    	request.setHeader(HttpHeaders.Names.HOST, this.getHost());
			
	    	handler.updateAPIClientConnectionListener(listener);
							
			logger.info("HERE======>writeRequest() TO CHANNEL: "+this.getChannel()+"\nHANDLER: "+this.getChannel().getPipeline().get("handler")+ " THREAD: " +Thread.currentThread().getId());
			this.getChannel().write(request);
			
	    }
	
		private static class HttpResponseHandler extends SimpleChannelUpstreamHandler  {
	    	
			private RemoteConnectionListener listener;
	    	private boolean isReceivingChuckedData;
	    	private boolean isReceivingDataFinished;
	    	private boolean isReadComplete;
	    	private boolean isReadSuccess;
	    	private boolean isWriteComplete;
	    	private boolean isWriteSuccess;
	    	/** used for caching the response data */
	        private HttpResponse response;
	    	private ChannelBuffer responseContent;
	    	private int responseContentSize;
	        private NettyRemoteConnection connection;
	        
	        
	    	public HttpResponseHandler(NettyRemoteConnection connection) {    		
	    		this.reset();
	    		this.connection=connection;
	    	}
	    	
	    	public void reset() {
	    		this.isReceivingChuckedData=false;
	    		this.isReceivingDataFinished=false;
	    		this.isReadComplete=false;
	    		this.isWriteComplete=false;
	    		this.response=null;
	    		this.responseContent = null;
	    		this.responseContentSize=0;
	    		this.listener=null;
	
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
							logger.warning(e1.getMessage());
						}		    			
		    		}
		    		
		    		return this.isWriteSuccess;
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
							logger.warning(e1.getMessage());
						}
		    			
		    			//exceeding the specified waiting time.
		    			if((System.currentTimeMillis()-startTime)>=timeout)
		    			{
		    				throw new RemoteConnectionException("", RemoteConnectionError.TIMEOUT);
		    			}
		    				    				    	
		    		}
		    		
		    		return this.isWriteSuccess;
				}    		
	    	}
	    	
	    	public HttpResponse getResponseData() {
	    		
	    		synchronized(this) {											
																
		    		while(!isReadComplete)
		    		{
		    			try {
		    				this.wait();	    					
						} catch (InterruptedException e1) {
							logger.warning(e1.getMessage());
						}
		    			
		    			//waked because of failed to get response data.(see exceptionCaught)
		    			//if(!isReadComplete) return null;
		    		}
		    		
		    		return this.getMessage();
				}    		
	    	}
	    	
	    	public HttpResponse getResponseData(long timeout) throws RemoteConnectionException {
	    		
	    		synchronized(this) {
	    			
					long startTime=timeout<0?0:System.currentTimeMillis();				
																
		    		while(!isReadComplete)
		    		{
		    			
		    			try {
		    				this.wait(timeout);	    					
						} catch (InterruptedException e1) {
							logger.warning(e1.getMessage());
						}
		    			
		    			//exceeding the specified waiting time.
		    			if((System.currentTimeMillis()-startTime)>=timeout)
		    			{
		    				throw new RemoteConnectionException("Timeout!", RemoteConnectionError.TIMEOUT);
		    			}
		    			
		    			//waked because of failed to get response data.(see exceptionCaught)
		    			//if(!isReadComplete) return null;	    				    		
		    		}
		    		
		    		return this.getMessage();
				}    		
	    	}
	    	
	    	private HttpResponse getMessage() {
	    		
	    		if(!this.isReadSuccess) return null;
	    		
	    		HttpResponse resp = new DefaultHttpResponse(this.response.getProtocolVersion(), this.response.getStatus());
	    		//int length = Integer.valueOf(this.response.getHeader(HttpHeaders.Names.CONTENT_LENGTH));
				for(String name: this.response.getHeaderNames())
				{
					resp.setHeader(name, this.response.getHeaders(name));
				}
				
	    		if(!this.response.isChunked())
	    		{
	    			resp.setContent(this.response.getContent().copy(0, this.response.getContent().capacity()));
	    		}
	    		else
	    		{
	    			resp.setContent(this.responseContent.copy(0, this.responseContentSize));
	    			this.responseContent.clear();
	    			this.responseContentSize = 0;
	    		}
	    		
	    		this.response.getContent().clear();
	    		this.response.clearHeaders();
	    		this.response = null;
	    		
	    		return resp;
	    	}
	    	
	    	private void finishReadingData(boolean isSuccess) {
	    		synchronized(this) {
	    			if(!isReadComplete)
	    			{
	    				this.isReadComplete=true;
		    			this.isReadSuccess=isSuccess;
	    			}
	    			logger.info("Finish Reading Data "+this.isReadSuccess+ " THREAD: " +Thread.currentThread().getId());
	    			this.notifyAll();
	    			logger.info("Finish Reading Data Notify All Waitiers"+ " THREAD: " +Thread.currentThread().getId());
	    		}
	    	}
	    	
	    	private void finishWritingData(boolean isSuccess) {
	    		synchronized(this) {
	    			if(!isWriteComplete)
	    			{
	    				this.isWriteComplete=true;
		    			this.isWriteSuccess=isSuccess;
	    			}
	    			logger.info("Finish Writing Data "+this.isWriteSuccess+ " THREAD: " +Thread.currentThread().getId());
	    			this.notifyAll();
	    			logger.info("Finish Writing Data Notify All Waitiers"+ " THREAD: " +Thread.currentThread().getId());
	    		}
	    	}
	    	
	    	@Override
	    	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
	    		logger.info(this+" exceptionCaught(): "+ " THREAD: " +Thread.currentThread().getId());
	    		logger.warning("Channel "+e.getChannel()+" occures error due to "+e.getCause().getMessage()+ " THREAD: " +Thread.currentThread().getId());
	
	    		Channel ch = e.getChannel();
	    		if(ch.isOpen())
	    			ch.close(); //try to close the channel, but do not waiting for it closed.
	    		
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
	    		
	    		if (!isReceivingChuckedData)
	    		{    		
	    			this.response = (HttpResponse) e.getMessage();
	    			
	    			logger.info("STATUS: " + this.response.getStatus()+"" +
	    					"\nVERSION: " + this.response.getProtocolVersion()+"\n");
	    			
	    			if (!this.response.getHeaderNames().isEmpty()) 
	    			{
	    				if(logger.isLoggable(Level.INFO))
	    				{
	    					StringBuilder headerInfo = new StringBuilder();
	    					for (String name: this.response.getHeaderNames()) 
		    				{
		    					for (String value: this.response.getHeaders(name)) 
		    					{
		    						headerInfo.append("HEADER: ");
		    						headerInfo.append(name);
		    						headerInfo.append(" = ");
		    						headerInfo.append(value);
		    						headerInfo.append("\n");		    						
		    					}
		    				}
	    					headerInfo.append("\n");
	    					logger.info("\n"+headerInfo.toString());
	    				}
	
	    			}
	    			    			
	    			//clean data
	    			//this.responseContent.clear();
	    			this.isReceivingDataFinished=false;
	    			
	    			if (this.response.isChunked()) 
	    			{
	    				this.responseContent = ChannelBuffers.dynamicBuffer();
	    				isReceivingChuckedData = true;
	    				logger.info("Chunked Response Data");
	    			} 
	    			else 
	    			{    				
	    				ChannelBuffer content = response.getContent();
	    				logger.info("Channel buffer capacity is "+content.capacity());
	    				
	    				if (content.readable()) 
	    				{
	    					//save data
	        				logger.info("CONTENT {"+content.toString()+"}");	        			
	    				}
	    				else
	    				{
	    					logger.info("Channel buffer is not readable!");
	    				}
	    				
	    				this.isReceivingDataFinished=true;
	    				
	    			}
	    		}
	    		else 
	    		{
	    			HttpChunk chunk = (HttpChunk) e.getMessage();
	    			if (chunk.isLast()) 
	    			{
	    				isReceivingChuckedData = false;
	    				this.isReceivingDataFinished=true;
	    				logger.info("} END OF CHUNKED CONTENT");
	    				
	    			} 
	    			else 
	    			{
	    				ChannelBuffer content = chunk.getContent();
	    				logger.info("CHUNKED CONTENT: "+content+ "; "+chunk);
	    				//save data
	    				//byte[] bytes = content.array();
	    				//counting actual size of content.
	    				this.responseContentSize+=content.readableBytes();
	    				logger.info("COUNTING CHUNKED CONTENT SIZE: "+this.responseContentSize);
	    				this.responseContent.writeBytes(content,content.readableBytes());
	    				logger.info("TOTAL CHUNKED CONTENT: "+this.responseContent);
	    				//logger.info("CONTENT: "+new String(bytes));
	    			}
	    		}//end of reading data
	    		
	    		if(this.isReceivingDataFinished)
	    		{	    			
	    			logger.info(this+" messageReceived(): CHANNEL: "+this.connection.getChannel()+ " THREAD: " +Thread.currentThread().getId());
	    			if(null!=listener)
					{
	    				logger.info("listener.readComplete(): CHANNEL: "+this.connection.getChannel()+ " THREAD: " +Thread.currentThread().getId());
	    				listener.readComplete(this.connection, true, this.getMessage()); //non-blocking.
	    				
					}
	    			else
	    			{
	    				logger.info("this.finishReadingData(): CHANNEL: "+this.connection.getChannel()+ " THREAD: " +Thread.currentThread().getId());
	    				finishReadingData(true); //blocking
	    			}
	    		}
	    	  			
	    	}
	    	
	    }
	}

}
