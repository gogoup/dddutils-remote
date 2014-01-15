package org.gogoup.dddutils.remote.impl;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gogoup.dddutils.remote.RemoteHttpRequest;
import org.gogoup.dddutils.remote.RemoteHttpResponse;

public class DefaultRemoteHttpResponse implements RemoteHttpResponse {
    
    private RemoteHttpRequest request;
    private String protocol;
    private int status;
    private String[] headerNames;
    private Map<String, String[]> headers; 
    private int contentCategory = 0;
    private String message;
    private File file;
    private ByteBuffer data;
    
    public DefaultRemoteHttpResponse(RemoteHttpRequest request, int status) {
        
        this.request = request;
        this.protocol = this.request.getProtocol();
        this.status = status;
        this.headerNames = null;
        this.headers = new LinkedHashMap<String, String[]>();
        this.contentCategory = NONE_CONTENT; 
        this.message = null;
        this.file = null;
        this.data = null;
    }
    
    @Override
    public RemoteHttpRequest getRequest() {
        return request;
    }

    @Override
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    @Override
    public String getProtocol() {
        return protocol;
    }
    
    @Override
    public void setStatus(int code) {
        this.status = code;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setHeader(String name, String[] values) {
        headers.put(name, values);
        this.headerNames = null;
    }
    
    @Override
    public String[] getHeaderNames() {
        if (null == headerNames) {
            headerNames = headers.keySet().toArray(new String[headers.size()]);
        }
        return headerNames;
    }
    
    @Override
    public String[] getHeaders(String name) {
        return headers.get(name);
    }
    
    @Override
    public String getHeader(String name) {
        String[] values = getHeaders(name);
        if (null == values || values.length == 0) {
            return null;
        }
        return values[0];
    }

    @Override
    public void writeMessage(String message) {  
        checkForNoContentAssigned();
        
        this.message = message;
        contentCategory = MESSAGE_CONTENT;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public void writeFile(File file) {
        checkForNoContentAssigned();
       
        this.file = file;
        contentCategory = FILE_CONTENT;
    }
    
    public File getFile() {
        return file;
    }

    @Override
    public void writeData(ByteBuffer data) {
        checkForNoContentAssigned();       
        
        this.data = data;
        contentCategory = DATA_CONTENT;
    }
    
    public ByteBuffer getData() {
        return data;
    }
    
    @Override
    public int getContentCategory() {
        return contentCategory;
    }
    
    private void checkForNoContentAssigned() {
        if (FILE_CONTENT == contentCategory) {
            throw new IllegalStateException("File has been assigned.");
        } 
        if (MESSAGE_CONTENT == contentCategory) {
            throw new IllegalStateException("Message has been assigned.");
        } 
        if (DATA_CONTENT == contentCategory) {
            throw new IllegalStateException("Data has been assigned.");
        }
    }

}
