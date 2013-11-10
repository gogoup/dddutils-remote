package org.gogoup.dddutils.remote.server.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gogoup.dddutils.appsession.AppSession;
import org.gogoup.dddutils.appsession.AppSessionFactory;
import org.gogoup.dddutils.appsession.NoSuchAppSessionException;
import org.gogoup.dddutils.remote.server.RemoteHttpMethod;
import org.gogoup.dddutils.remote.server.RemoteHttpResponse;
import org.gogoup.dddutils.remote.server.RemoteHttpService;
import org.gogoup.dddutils.remote.server.SessionParameterNames;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.util.CharsetUtil;

public class NettyHttpChannelHandler extends IdleStateAwareChannelHandler  {

	private final static Logger logger = Logger.getLogger(NettyHttpChannelHandler.class.getCanonicalName());
	
    private HttpRequest request;
    private boolean startReadingChunks;
    private boolean readingFinished;
    private Map<String, String[]> queryParameters;
    private Map<String,String[]> headers;
    private String path;
    private RemoteHttpMethod httpMethod;
    private ChannelBuffer requestData;
    private Integer currentChannelId;
    private AppSessionFactory sessionFactory;
    private ExecutorService sessionExecutorPool;
    private AppSessionRunner sessionRunner;
    private DefaultHttpServer server;
    
    
    public NettyHttpChannelHandler(DefaultHttpServer server) {
    	this.server=server;
    	startReadingChunks=false;
    	readingFinished=false;
    	this.currentChannelId = null;    	
    	this.sessionFactory=this.server.getSessionFactory();
    	this.sessionExecutorPool=this.server.getSessionExecutorPool();
    }
    
    private AppSessionFactory getSessionFactory() {
    	return sessionFactory;
    }

    //the following part is only used for looking into the message flow between netty channels in a pipeline.
//    @Override
//    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
//
//    	System.out.println("HERE======>handleDownstream() CHANNEL ID: "+ctx.getChannel().getId()+ "; THREAD: " +Thread.currentThread().getId());
//    	System.out.println("HERE======>handleDownstream() EVENT: "+e+ "; THREAD: " +Thread.currentThread().getId());
//
//        super.handleDownstream(ctx, e);
//    }
//    
//    @Override
//    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
//
//    	System.out.println("HERE======>handleUpstream() CHANNEL ID: "+ctx.getChannel().getId()+ "; THREAD: " +Thread.currentThread().getId());
//    	System.out.println("HERE======>handleUpstream() EVENT: "+e+ "; THREAD: " +Thread.currentThread().getId());
//
//        super.handleUpstream(ctx, e);
//    }
	
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    	
    	//System.out.println("\nHERE======>messageReceived() CHANNEL ID: "+ctx.getChannel().getId()+ "; THREAD: " +Thread.currentThread().getId());
    	
    	if(null == this.currentChannelId)
    		this.currentChannelId = ctx.getChannel().getId();
    			
        if (!startReadingChunks) {
        	
            HttpRequest request = this.request = (HttpRequest) e.getMessage();
            this.headers = new HashMap<String, String[]>();
        	this.queryParameters = new HashMap<String, String[]>();
            this.httpMethod = RemoteHttpMethod.toRemoteHttpMethod(request.getMethod().getName());
            this.requestData = null;
            //logging
            StringBuilder logStrBuilder = null;
            if(logger.isLoggable(Level.INFO))
            {
            	logStrBuilder = new StringBuilder();
            	logStrBuilder.append("VERSION: " + request.getProtocolVersion());
            	logStrBuilder.append("\n");
            	logStrBuilder.append("\n");
            	
            	logStrBuilder.append("REQUEST METHOD: " + httpMethod.getName());
            	logStrBuilder.append("\n");
            	logStrBuilder.append("\n");
            }
			//http headers
			if (!request.getHeaderNames().isEmpty()) 
			{
				for (String name: request.getHeaderNames()) 
				{
					List<String> values = request.getHeaders(name);
					headers.put(name, values.toArray(new String[values.size()]));
				}
			}
			//logging
			if(null != logStrBuilder) 
			{
				logStrBuilder.append(headers);
				logStrBuilder.append("\n");
			}
			
            if (HttpHeaders.is100ContinueExpected(request)) {
                send100Continue(e);
            }
                        
            //path
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
            this.path = queryStringDecoder.getPath();
            
            //url query parameters.
            Map<String,List<String>> parameters = queryStringDecoder.getParameters();
            for(Iterator<String> iter = parameters.keySet().iterator(); iter.hasNext();)
            {
            	String paramName = iter.next();
            	List<String> paramValues = parameters.get(paramName);
            	this.queryParameters.put(paramName, paramValues.toArray(new String[paramValues.size()]));
            }
            //logging
            if(null != logStrBuilder)
            {
            	String uri = request.getUri();
            	logStrBuilder.append("URI: "+uri);
            	logStrBuilder.append("\n");
                logStrBuilder.append("PATH: "+path);
                logStrBuilder.append("\n");                
                logStrBuilder.append("PARAMETERS: "+queryParameters.toString());
                logStrBuilder.append("\n");
                logger.info(logStrBuilder.toString());                
            }
            //process request data
            if (request.isChunked()) 
            {
                startReadingChunks = true;
            } 
            else 
            {
                this.requestData = request.getContent();
                readingFinished=true;
            }
            
        } else {
        	//chunked request data.
            HttpChunk chunk = (HttpChunk) e.getMessage();
            if(null == this.requestData)
            	this.requestData = chunk.getContent();
            else
            	this.requestData.writeBytes(chunk.getContent());
            if (chunk.isLast()) 
            {                   
                readingFinished=true;
            } 
        }
        
        if(readingFinished)
        {            	            	            	            	
        	//reset reading status.
        	startReadingChunks=false;
        	readingFinished=false;
        	
        	logger.info("HERE======>COMING IN PARAMETERS: "+queryParameters+"; THREAD: "+Thread.currentThread().getId());
        	//http keep alive header
        	boolean keepAlive = HttpHeaders.isKeepAlive(request);
        	String sessionKey = this.server.getDelegate().getSessionKey(this.httpMethod, headers, queryParameters);
			
			//non-blocking way to process service request.
			this.sessionRunner = new AppSessionRunner(e.getChannel(), this.getSessionFactory(), sessionKey,
					this.httpMethod, keepAlive, this.path, headers, queryParameters, this.requestData);
			this.sessionExecutorPool.execute(this.sessionRunner);
				
        }			
    }

    /**
     * This method is hardcoded for http response. (need to be refactored)
     * 
     * @param channel
     * @param responseData
     * @param sessionRunner
     */
    private static void writeResponse(Channel channel, String responseData, final AppSessionRunner sessionRunner) {
    	
    	//System.out.println("HERE======>RemoteHttpRequestHandler.writeResponse() #1: "+responseData+"; THREAD: "+Thread.currentThread().getId());
        //Decide whether to close the connection or not.
        //boolean keepAlive = HttpHeaders.isKeepAlive(request);

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        //response.setChunked(true);
        response.setContent(ChannelBuffers.copiedBuffer(responseData, CharsetUtil.UTF_8));
        response.setHeader(HttpHeaders.Names.ACCEPT_RANGES, "bytes");
        //response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
        //response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/javascript; charset=UTF-8");
        //response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plan; charset=UTF-8");
        //response.setHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
        
        //setup response for CORS (cross original resource share)
        response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST");
        //response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_METHODS, "GET, PUT, POST, DELETE, OPTIONS");
        response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_MAX_AGE, "604800");
        //response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type");
        response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_HEADERS, "x-requested-with");
        
        if (sessionRunner.isKeepAlive()) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, response.getContent().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
//        System.out.println("HERE======>RemoteHttpRequestHandler.writeResponse() #2 \n"+response);
        
        // Encode the cookie.
//        String cookieString = request.getHeader(HttpHeaders.Names.COOKIE);
//        
//        if (cookieString != null) {
//            CookieDecoder cookieDecoder = new CookieDecoder();
//            Set<Cookie> cookies = cookieDecoder.decode(cookieString);
//            if (!cookies.isEmpty()) {
//                // Reset the cookies if necessary.
//                CookieEncoder cookieEncoder = new CookieEncoder(true);
//                for (Cookie cookie : cookies) {
//                    cookieEncoder.addCookie(cookie);
//                }
//                response.addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
//            }
//        }
        
        // Write the response.
        ChannelFuture future = channel.write(response);
        future.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
//				System.out.println("HERE======>RemoteHttpRequestHandler.writeResponse() FUTURE IS DONE: "+future.isDone()+" - CHANNEL: "+future.getChannel().getId()+ "; THREAD: " +Thread.currentThread().getId());
//				System.out.println("HERE======>RemoteHttpRequestHandler.writeResponse() FUTURE IS CANCELLED: "+future.isCancelled()+" - CHANNEL: "+future.getChannel().getId()+ "; THREAD: " +Thread.currentThread().getId());
//				System.out.println("HERE======>RemoteHttpRequestHandler.writeResponse() FUTURE IS SUCCESS: "+future.isSuccess()+" - CHANNEL: "+future.getChannel().getId()+ "; THREAD: " +Thread.currentThread().getId());
				if(future.isDone() && future.isSuccess())
					sessionRunner.done();				
			}
        	
        });
       
        // Close the non-keep-alive connection after the write operation is done.
        if (!sessionRunner.isKeepAlive()) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static void send100Continue(MessageEvent e) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        e.getChannel().write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
    	//System.out.println("\nHERE======>RemoteHttpRequestHandler.exceptionCaught() CAUSE: "+e.getCause()+ "; THREAD: " +Thread.currentThread().getId());
    	if(ctx.getChannel().getId() == this.currentChannelId)
		{
    		this.sessionRunner.exceptionCaught(e.getCause());
        	this.currentChannelId = null;
		}
    	
    	//e.getCause().printStackTrace();
    }
    
    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
    	
		if (e.getState() == IdleState.READER_IDLE
				|| e.getState() == IdleState.WRITER_IDLE
				|| e.getState() == IdleState.WRITER_IDLE) 
		{
			
			if(ctx.getChannel().getId() == this.currentChannelId)
			{
				this.sessionRunner.channelIdle();
	        	this.currentChannelId = null;
			}
			
			ChannelFuture future = ctx.getChannel().close();
			future.addListener(new ChannelFutureListener(){

				@Override
				public void operationComplete(ChannelFuture future)
						throws Exception {
				}
				
			});
			
        } 
    }
    
    private static class AppSessionRunner implements Runnable {
    	
    	private static final int STATUS_OK = 0;
    	private static final int STATUS_EXCEPTION = 0x01 << 0;
    	private static final int STATUS_IDLE = 0x01 << 1;
    	
    	private Channel channel;
    	private AppSessionFactory sessionFactory;
    	private String sessionKey;
    	private AppSession session;
    	private RemoteHttpMethod httpMethod;
    	private boolean keepAlive;
    	private String path;
    	private Map<String, String[]> headers;
    	private Map<String, String[]> queryParameters;
    	private int status;
    	private RemoteHttpResponseImpl remoteHttpResponse;
    	private RemoteHttpService remoteHttpService;
    	private ChannelBuffer requestData;
    	
    	public AppSessionRunner(Channel channel, AppSessionFactory sessionFactory, String sessionKey,
    			RemoteHttpMethod httpMethod, boolean keepAlive, String path, Map<String, String[]> headers, 
    			Map<String, String[]> queryParameters, ChannelBuffer requestData) {
    		
    		this.channel=channel;
    		this.sessionFactory=sessionFactory;
    		this.sessionKey=sessionKey;
    		this.httpMethod=httpMethod;
    		this.keepAlive=keepAlive;
    		this.path=path;
    		this.headers=headers;
    		this.queryParameters=queryParameters;  
    		this.requestData=requestData;
    		this.status = STATUS_OK;
    	}

		public Channel getChannel() {
			return channel;
		}

		public boolean isKeepAlive() {
			return keepAlive;
		}
				

		@Override
		public void run() {
			
			//fetch a AppSession from pool based on the giving session key.
			try {
				this.session = this.sessionFactory.getSession(this.sessionKey);
			} catch (NoSuchAppSessionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//update this session by restore states from persistence layer 
			// or create a new in-memory states (as persistence class not defined).
			try {
				this.session.update();
			} catch (NoSuchAppSessionException e1) {
				e1.printStackTrace();
			}
			//setting necessary parameters into session.
			this.session.setParameter(SessionParameterNames.HTTP_HEADER, headers);
			this.session.setParameter(SessionParameterNames.URL_PARAMETER, queryParameters);
			this.session.setParameter(SessionParameterNames.URL_PATH, this.path.replace("/", ""));
			this.session.setParameter(SessionParameterNames.HTTP_REQUEST_DATA, requestData);
		
			Object[] services = this.session.findParameters(this.path);
        	if(services.length == 0)
        	{
        		this.session.close();
        		writeResponse(this.getChannel(), "No such service found for "+this.path, this);
        		//this.exceptionCaught(new RuntimeException("No such service found for "+this.path));
        		return;
        	}
        	//only the first service will be used to process the request.
        	this.remoteHttpService = (RemoteHttpService) services[0];        	
        	this.remoteHttpResponse=new RemoteHttpResponseImpl(this);
        	//start process
        	this.remoteHttpService.process(httpMethod, this.session, this.remoteHttpResponse);
        	
		}
		
		public synchronized void done() {
			
			this.status = STATUS_OK;
			if(null != this.remoteHttpService)
			{
				this.remoteHttpService.done(session);
				
				this.channel = null;
				this.session = null;
			}
			
		}

		public synchronized void exceptionCaught(Throwable exception) {
			//System.out.println("HERE======>AppSessionRunner.exceptionCaught() #1 "+"; THREAD: "+Thread.currentThread().getId());	
			this.status = this.status | STATUS_EXCEPTION;
			//System.out.println("HERE======>AppSessionRunner.exceptionCaught() #2 "+"; THREAD: "+Thread.currentThread().getId());
			if(null != this.remoteHttpService)
			{
				//System.out.println("HERE======>AppSessionRunner.exceptionCaught() #1.1 SERVICE: "+this.remoteHttpService+"; THREAD: "+Thread.currentThread().getId());	
				this.remoteHttpService.exceptionOccured(exception, this.session, this.remoteHttpResponse);
				//System.out.println("HERE======>AppSessionRunner.exceptionCaught() #1.2 SERVICE: "+this.remoteHttpService+"; THREAD: "+Thread.currentThread().getId());
				this.channel = null;
				this.session = null;
			}
		}
    	
		public synchronized void channelIdle() {
			this.status = this.status | STATUS_IDLE;
			if(null != this.remoteHttpService)
			{
				this.remoteHttpService.connectionClosed(this.session);
				this.channel = null;
				this.session = null;
			}		
		}
		
    }
    
    private static class RemoteHttpResponseImpl implements RemoteHttpResponse {

    	private AppSessionRunner sessionRunner;    	    
    	
    	public RemoteHttpResponseImpl(AppSessionRunner sessionRunner) {    		
    		this.sessionRunner=sessionRunner;    		
    	}
    	
		@Override
		public void writeResponseMessage(String message) {
			
			if(null == message) message = "";
			writeResponse(this.sessionRunner.getChannel(), message, this.sessionRunner);
	    		   	
		}

		@Override
		public void handleException(Throwable exception) {
			//System.out.println("HERE======>RemoteHttpResponseImpl.throwException() #1: exception: "+exception+ "; THREAD: " +Thread.currentThread().getId());
			//this.sessionRunner.exceptionCaught(exception);
			writeResponse(this.sessionRunner.getChannel(), exception.getMessage(), this.sessionRunner);
		}
    	
    }
}
