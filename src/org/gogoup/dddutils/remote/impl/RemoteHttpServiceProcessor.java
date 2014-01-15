package org.gogoup.dddutils.remote.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.activation.MimetypesFileTypeMap;

import org.gogoup.dddutils.appsession.AppSession;
import org.gogoup.dddutils.appsession.AppSessionContext;
import org.gogoup.dddutils.appsession.AppSessionFactory;
import org.gogoup.dddutils.appsession.NoSuchAppSessionException;
import org.gogoup.dddutils.remote.RemoteHttpRequest;
import org.gogoup.dddutils.remote.RemoteHttpResponse;
import org.gogoup.dddutils.remote.RemoteHttpService;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.FileRegion;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.jboss.netty.util.CharsetUtil;

public class RemoteHttpServiceProcessor implements Runnable{
    
    private Channel channel;
    private RemoteHttpRequest request;
    private AppSessionFactory application;
    private AppSession originalSession;
    private AppSessionWrapper wrappedSession;
    private DefaultRemoteHttpResponse response;
    
    public RemoteHttpServiceProcessor(Channel channel, RemoteHttpRequest request, 
            AppSessionFactory application, AppSession session) {

        this.channel = channel;
        this.request = request;
        this.application = application;
        this.wrappedSession = null;
        this.originalSession = session;
    }
  
    @Override
    public void run() {
        
        this.response = new DefaultRemoteHttpResponse(request, HttpResponseStatus.OK.getCode());

        if (null == this.application) {
            this.response.writeMessage("Invalid service request.");
            writeResponse(this.response);
            return;
        }
        
        if (null == this.originalSession) {
            this.response.writeMessage("Invalid access session!");
            writeResponse(this.response);
            return;
        }
        
        this.wrappedSession = new AppSessionWrapper(this.originalSession);
        try {
            this.wrappedSession.update();
        } catch (NoSuchAppSessionException e) {            
            throwException(e);
            return;
        }
       
        setCORSResonseHeaders(this.wrappedSession, this.response);
        
        Object[] services = this.wrappedSession.findParameters(request.getPath());            
        if (services.length == 0) {
            this.response.writeMessage("No such service found, " + request.getPath());
            writeResponse(this.response);
            return;
        }
        
        try {
            
            RemoteHttpService service = (RemoteHttpService) services[0]; // only the first found service.
            service.process(request, this.response);
            wrappedSession.sync();
            completed();
            
        } catch (Exception e) {
            throwException(e);
            return;
        }
    }
    
    public synchronized void completed() {
        writeResponse(this.response);
    }
    
    public synchronized void throwException(Throwable exception) {
        exception.printStackTrace(System.out);
        this.response.writeMessage(exception.getMessage());
        this.response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.getCode());
        writeResponse(this.response);
    }
    
    public synchronized void lostConnection() {
        this.response.writeMessage("Connection closed.");
        this.response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.getCode());
        writeResponse(this.response);
    }
    
    public void shutdown() {
        this.channel = null;
        this.request = null;
        this.application = null;
        this.originalSession = null;
        if (null != wrappedSession) {
            wrappedSession.close();
        }
        this.response = null;
    }
    
    private void setCORSResonseHeaders(AppSession session, DefaultRemoteHttpResponse response) {
        AppSessionContext sessionCtx = session.getAppSessionContext();
        String origin = (String) sessionCtx.getApplicationParameter(
                HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN);
        if (null != origin) {
            response.setHeader(
                    HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, new String[]{origin});
        }
        String credentials  = (String) sessionCtx.getApplicationParameter(
                HttpHeaders.Names.ACCESS_CONTROL_ALLOW_CREDENTIALS);
        if (null != credentials) {
            response.setHeader(
                    HttpHeaders.Names.ACCESS_CONTROL_ALLOW_CREDENTIALS, new String[]{credentials});
        }
        String headers  = (String) sessionCtx.getApplicationParameter(
                HttpHeaders.Names.ACCESS_CONTROL_ALLOW_HEADERS);
        if (null != headers) {
            response.setHeader(
                    HttpHeaders.Names.ACCESS_CONTROL_ALLOW_HEADERS, new String[]{headers});
        }
        String maxAge  = (String) sessionCtx.getApplicationParameter(
                HttpHeaders.Names.ACCESS_CONTROL_MAX_AGE);
        if (null != maxAge) {
            response.setHeader(
                    HttpHeaders.Names.ACCESS_CONTROL_MAX_AGE, new String[]{maxAge});
        }
        String methods  = (String) sessionCtx.getApplicationParameter(
                HttpHeaders.Names.ACCESS_CONTROL_ALLOW_METHODS);
        if (null != methods) {
            response.setHeader(
                    HttpHeaders.Names.ACCESS_CONTROL_ALLOW_METHODS, new String[]{methods});
        }
        String expHeaders  = (String) sessionCtx.getApplicationParameter(
                HttpHeaders.Names.ACCESS_CONTROL_EXPOSE_HEADERS);
        if (null != expHeaders) {
            response.setHeader(
                    HttpHeaders.Names.ACCESS_CONTROL_EXPOSE_HEADERS, new String[]{expHeaders});
        }        
    }

    private void setHeaders(DefaultRemoteHttpResponse response, HttpResponse httpResponse) {
        String[] headerNames = response.getHeaderNames();
        for (String name: headerNames) {
            String[] values = response.getHeaders(name);
            if (null == values) {
                continue;
            }
            StringBuilder valueString = new StringBuilder();
            for (String value: values) {
                valueString.append(value);
                valueString.append(",");
            }
            //remote the last common
            if (valueString.length() > 0) {
                valueString.deleteCharAt(valueString.length() - 1);
            }
            httpResponse.setHeader(name, valueString.toString());
        }
    }
    
    private void prepareForMessageContent(DefaultRemoteHttpResponse response, HttpResponse httpResponse) { 
        String message = response.getMessage();
        if (null == message) {
            message = "";
        }
        ChannelBuffer content = ChannelBuffers.copiedBuffer(message, CharsetUtil.UTF_8);        
        httpResponse.setContent(content);
        httpResponse.setHeader(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());        
        if (!httpResponse.containsHeader(HttpHeaders.Names.CONTENT_TYPE)) {
            httpResponse.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");   
        }           
    }
    
    private void prepareForDataContent(DefaultRemoteHttpResponse response, HttpResponse httpResponse) {        
        ChannelBuffer content = ChannelBuffers.copiedBuffer(response.getData());        
        httpResponse.setContent(content);
        httpResponse.setHeader(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
        if (!httpResponse.containsHeader(HttpHeaders.Names.CONTENT_TYPE)) {
            httpResponse.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/octet-stream");   
        }
        if (!httpResponse.containsHeader(HttpHeaders.Names.TRANSFER_ENCODING)) {
            httpResponse.setHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.BINARY); 
        }       
    }
    
    private void prepareForFileContent(DefaultRemoteHttpResponse response, HttpResponse httpResponse) {                    
        httpResponse.setHeader(HttpHeaders.Names.CONTENT_LENGTH, response.getFile().length());
        if (!httpResponse.containsHeader("Content-Disposition")) {
            httpResponse.setHeader("Content-Disposition",
                    "attachment; filename='" + response.getFile().getName() +"'");
        }
        
        if (!httpResponse.containsHeader(HttpHeaders.Names.CONTENT_TYPE)) {
            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            httpResponse.setHeader(HttpHeaders.Names.CONTENT_TYPE, 
                    mimeTypesMap.getContentType(response.getFile()));   
        }
    }

    private void writeResponse(DefaultRemoteHttpResponse response) {
        HttpVersion protocol = getHttpVersion(response.getProtocol());
        HttpResponseStatus status = HttpResponseStatus.valueOf(response.getStatus());
            
        HttpResponse httpResponse = new DefaultHttpResponse(protocol, status);
        setHeaders(response, httpResponse);
        
        if (response.getContentCategory() == RemoteHttpResponse.MESSAGE_CONTENT) {
            prepareForMessageContent(response, httpResponse);
            writeResponse(httpResponse);
        } else if (response.getContentCategory() == RemoteHttpResponse.DATA_CONTENT) {
            prepareForDataContent(response, httpResponse);
            writeResponse(httpResponse);
        } else if (response.getContentCategory() == RemoteHttpResponse.FILE_CONTENT) {
            prepareForFileContent(response, httpResponse);
            writeResponse(httpResponse, response.getFile());
        } else {
            writeResponse(httpResponse);
        }
    }
   
    private void writeResponse(HttpResponse response) {
        
        if (!channel.isConnected()) {
            System.err.println("Channel, "+channel+" has been closed!"
                    + "; THREAD: " +Thread.currentThread().getId());
            wrappedSession.close();
            return;
        }
        
        ChannelFuture future = channel.write(response);
        future.addListener(new RemoteChannelFutureListener(this));
        
        //Close the non-keep-alive connection after the write operation is done.
        if (!HttpHeaders.isKeepAlive(response)) {
            future.addListener(ChannelFutureListener.CLOSE);
            wrappedSession.close();
        }
        
    }
    
    private void writeResponse(HttpResponse response, File file) {
        RandomAccessFile raf = null;
        long fileLength = 0;
        try {
            raf = new RandomAccessFile(file, "r");
            fileLength = raf.length();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (!channel.isConnected()) {
            System.err.println("Channel, "+channel+" has been closed!"
                    + "; THREAD: " +Thread.currentThread().getId());
            wrappedSession.close();
            return;
        }
        
        channel.write(response);
        
        ChannelFuture future = null;
        
        try {
            if (channel.getPipeline().get(SslHandler.class) != null) {
                ChunkedFile checkedFile = new ChunkedFile(raf, 0, fileLength, 8192);
                future = channel.write(checkedFile);
                future.addListener(new ChunkedFileChannelFutureListener(this, checkedFile));
            } else {
                FileRegion region = new DefaultFileRegion(raf.getChannel(), 0, fileLength);
                future = channel.write(region); 
                future.addListener(new FileRegionChannelFutureListener(this, region));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
        
        //Close the non-keep-alive connection after the write operation is done.
        if (!HttpHeaders.isKeepAlive(response)) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
    
    private static HttpVersion getHttpVersion(String text) {
        
        if (text.equalsIgnoreCase("HTTP_1_0")) {
            return HttpVersion.HTTP_1_0;
        } else if (text.equalsIgnoreCase("HTTP_1_1")) {
            return HttpVersion.HTTP_1_1;
        } else {
            return null;
        }
    }
    
// setup response for CORS (cross original resource share)
//  response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
//  response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST");
//  // response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_METHODS,
//  // "GET, PUT, POST, DELETE, OPTIONS");
//  response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_MAX_AGE, "604800");
//  // response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_HEADERS,
//  // "Content-Type");
//  response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_HEADERS, "x-requested-with");
//  
    
    private static class RemoteChannelFutureListener implements ChannelFutureListener {
        
        private RemoteHttpServiceProcessor processor;
        
        public RemoteChannelFutureListener(RemoteHttpServiceProcessor processor) {
            this.processor = processor;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isDone()) {
                processor.shutdown();
            }
        }
        
    }

    private static class ChunkedFileChannelFutureListener extends RemoteChannelFutureListener {

        private final ChunkedFile checkedFile;
        
        public ChunkedFileChannelFutureListener(RemoteHttpServiceProcessor processor, ChunkedFile checkedFile) {
            super(processor);
            this.checkedFile = checkedFile;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {

            if (future.isDone()) {
                this.checkedFile.close();
            }
            super.operationComplete(future);
        }
        
    }
    
    private static class FileRegionChannelFutureListener extends RemoteChannelFutureListener {
     
        private final FileRegion region;
        
        public FileRegionChannelFutureListener(RemoteHttpServiceProcessor processor, FileRegion region) {
            super(processor);
            this.region = region;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isDone()) {
                this.region.releaseExternalResources();
            }
            super.operationComplete(future);
        }
        
    }
}