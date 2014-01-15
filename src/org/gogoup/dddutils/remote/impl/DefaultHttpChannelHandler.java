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

package org.gogoup.dddutils.remote.impl;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.gogoup.dddutils.appsession.AppSession;
import org.gogoup.dddutils.appsession.AppSessionFactory;
import org.gogoup.dddutils.appsession.NoSuchAppSessionException;
import org.gogoup.dddutils.remote.RemoteHttpServer;
import org.gogoup.dddutils.remote.RemoteHttpServerConfig;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.multipart.Attribute;
import org.jboss.netty.handler.codec.http.multipart.FileUpload;
import org.jboss.netty.handler.codec.http.multipart.HttpDataFactory;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder.IncompatibleDataDecoderException;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;

public class DefaultHttpChannelHandler extends IdleStateAwareChannelHandler {
    
//    private final static Logger logger = Logger.getLogger(DefaultHttpChannelHandler.class.getCanonicalName());
    
    private final HttpDataFactory httpDataFactory;  // Disk if size exceed MINSIZE
    
    //private HttpRequest request;
    private boolean readingChunks;    
    private ChannelBuffer requestData;
    private boolean isMultiPartPost;
    private HttpPostRequestDecoder decoder;
    private DefaultRemoteHttpRequest remoteHttpRequest;    
    private RemoteHttpServer server;
    private RemoteHttpServerConfig serverConfig;
    private ExecutorService executorService;
    private RemoteHttpServiceProcessor serviceProcessor;    
    
    public DefaultHttpChannelHandler(RemoteHttpServer server, ExecutorService executorService, 
            HttpDataFactory httpDataFactory) {
        this.server = server;
        this.serverConfig = server.getConfig();
        this.httpDataFactory = httpDataFactory;
        this.readingChunks = false;         
        this.executorService = executorService;
        this.serviceProcessor = null;
    }
    
    //the following part is only used for looking into the message flow between netty channels in a pipeline.
//    @Override
//    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
//    
//        //System.out.println("HERE======>handleDownstream() CHANNEL ID: "+ctx.getChannel().getId()+ "; THREAD: " +Thread.currentThread().getId());
//        System.out.println("HERE======>handleDownstream() EVENT: "+e+ "; THREAD: " +Thread.currentThread().getId());
//    
//        super.handleDownstream(ctx, e);
//    }
//      
//    @Override
//    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
//    
//        //System.out.println("HERE======>handleUpstream() CHANNEL ID: "+ctx.getChannel().getId()+ "; THREAD: " +Thread.currentThread().getId());
//        System.out.println("HERE======>handleUpstream() EVENT: "+e+ "; THREAD: " +Thread.currentThread().getId());
//    
//        super.handleUpstream(ctx, e);
//    }
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent msg) throws Exception {
        
//        System.out.println("\nHERE======>messageReceived() CHANNEL ID: "+ctx.getChannel().getId()+"; THREAD: " +Thread.currentThread().getId());
       
        if (!this.readingChunks) {
            
            HttpRequest request = (HttpRequest) msg.getMessage();
                                   
            if (HttpHeaders.is100ContinueExpected(request)) {
                System.out.println("CONTINUE...");
                send100Continue(msg.getChannel());
            }            
                
            //create RemoteHttpRequest
            remoteHttpRequest = new DefaultRemoteHttpRequest(request);
            
            if(request.getMethod() == HttpMethod.POST) {
                //check if content length header missing (code 411)
                if (!request.containsHeader(HttpHeaders.Names.CONTENT_LENGTH)) {
                    sendLengthRequired(msg.getChannel());
                    return;
                }
                
                //check if request entity too large (code 413)
                long contentLength = HttpHeaders.getContentLength(request);
                if (contentLength > this.serverConfig.getMaxPostSize()) {
                    sendRequestEntityTooLarge(msg.getChannel());
                    return;
                }
                
                this.readIsMultiPartPost(request); // multiPart-Form post.                
                this.readingChunks = request.isChunked();
                
                if (this.isMultiPartPost) {
                    this.startReadingMultiPartHttpPostData(request);
                    if (!this.readingChunks) {
                        this.finishReadingMultiPartHttpPostData();   
                    }
                } else {
                    this.startReadingHttpPostData(request);
                    if (!this.readingChunks) {
                        this.finishReadingHttpPostData();
                    }
                }
            } else {
                this.processRequestInNewThread(msg.getChannel());
            }
            
        } else {
            // Chucked request data.
            HttpChunk chunk = (HttpChunk) msg.getMessage();
            
            if (this.isMultiPartPost) { 
                this.readMultiPartHttpDataChunk(chunk);
            } else {
                this.readHttpDataChunk(chunk);
            }
            
            if (chunk.isLast()) {
                if (this.isMultiPartPost) {
                    this.finishReadingMultiPartHttpPostData();
                } else {
                    this.finishReadingHttpPostData();
                }
                this.readingChunks = false;                
                this.processRequestInNewThread(msg.getChannel());
            }            
        }
        
    }
    
    private void processRequestInNewThread(Channel channel) {
        String applicationId = this.server.getDelegate().getApplicationId(remoteHttpRequest);
        String sessionKey = this.server.getDelegate().getSessionKey(remoteHttpRequest, applicationId);
        
        AppSessionFactory application = null;
        AppSession session = null;
        
        if (null != applicationId) {
            application = this.server.getApplication(applicationId);
        }
        
        if (null != application
                && null != sessionKey) {
            try {
                session = application.getSession(sessionKey);
            } catch (NoSuchAppSessionException e) {
                // Do nothing.
            }
        }
        serviceProcessor = new RemoteHttpServiceProcessor(channel, remoteHttpRequest, application, session);
        this.executorService.execute(serviceProcessor); // run in new thread.
    }
     
    private void readIsMultiPartPost(HttpRequest request) {
        String contentType = request.getHeader(HttpHeaders.Names.CONTENT_TYPE);
        if (null != contentType) {
            this.isMultiPartPost = contentType.contains(HttpHeaders.Values.MULTIPART_FORM_DATA);
        } else {
            this.isMultiPartPost = false;
        }
                
    }
    
    private void startReadingHttpPostData(HttpRequest request) {
        if (!request.isChunked()) {
            this.requestData = request.getContent();
        } else {
            String length = request.getHeader(HttpHeaders.Names.CONTENT_LENGTH);
            this.requestData = ChannelBuffers.buffer(Integer.valueOf(length));
        }
        
    }
    
    private void startReadingMultiPartHttpPostData(HttpRequest request) {
        try {
            decoder = new HttpPostRequestDecoder(httpDataFactory, request);
        } catch (ErrorDataDecoderException e1) {
            e1.printStackTrace();
        } catch (IncompatibleDataDecoderException e1) {
            e1.printStackTrace();
        }
    }
    
    private void readHttpDataChunk(HttpChunk chunk) {        
        this.requestData.writeBytes(chunk.getContent());
    }
    
    private void readMultiPartHttpDataChunk(HttpChunk chunk) {
        try {
            decoder.offer(chunk);
        } catch (ErrorDataDecoderException e) {
            e.printStackTrace();
        }
    }
    
    private void finishReadingHttpPostData() {
        this.remoteHttpRequest.setData(this.requestData);
    }
    
    /**
     * Reading all InterfaceHttpData from finished transfer
     */
    private void finishReadingMultiPartHttpPostData() {
        
        try {
            while (this.decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    readHttpData(data);                    
                }
            }
            
            decoder.cleanFiles();
        } catch (EndOfDataDecoderException e) {
            // End of post data.
        }
    }
  
    private void readHttpData(InterfaceHttpData data) {        
        if (data.getHttpDataType() == HttpDataType.Attribute) {
            
            Attribute attribute = (Attribute) data;            
            try {                
                this.remoteHttpRequest.setAttribute(attribute.getName(), attribute.getValue());                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
           
        } else if (data.getHttpDataType() == HttpDataType.FileUpload) {
            
            FileUpload fileUpload = (FileUpload) data;  
            
            if (fileUpload.isCompleted()) {
                try {                   
                    this.remoteHttpRequest.setUploadFile(fileUpload.getName(), 
                            fileUpload.getFilename(), fileUpload.getFile());                    
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }           
        }
    }
    
    private static void send100Continue(Channel channel) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        channel.write(response);
    }
    
    private static void sendLengthRequired(Channel channel) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.LENGTH_REQUIRED);
        channel.write(response);
    }
    
    private static void sendRequestEntityTooLarge(Channel channel) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE);
        channel.write(response).awaitUninterruptibly(10000); // 10s
        channel.close();
    }
    
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
//        System.out.println("\nHERE======>DefaultHttpChanneltHandler.exceptionCaught() CAUSE: "+e.getCause()
//                +"; Thread: " +Thread.currentThread().getId());
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
    
    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
        
        if (e.getState() == IdleState.READER_IDLE) {
            e.getChannel().close();
        } else if (e.getState() == IdleState.WRITER_IDLE) {
            System.out.println("HERE======>WRITER IDEL!");                       
            e.getChannel().close();
        }
        
    }
}
