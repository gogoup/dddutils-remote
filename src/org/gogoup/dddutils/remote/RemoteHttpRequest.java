package org.gogoup.dddutils.remote;

import java.io.File;
import java.nio.ByteBuffer;

public interface RemoteHttpRequest {
    
    public String getProtocol();
    
    public RemoteHttpMethod getMethod();
    
    public boolean isKeepAlive();
    
    public boolean hasHeader(String name);
    
    public String[] getHeaderNames();
    
    public String[] getHeaders(String name);
    
    public String getHeader(String name);
    
    public boolean hasQueryParameter(String name);
    
    public String[] getQueryParameterNames();

    public String[] getQueryParameters(String name);

    public String getQueryParameter(String name);

    public ByteBuffer getData();
    
    public String getPath();
    
    public boolean hasAttribute(String name);
    
    public String[] getAttributeNames();
    
    public String getAttribute(String name);
    
    public String[] getFileUploadNames();
    
    public String getUploadFileNames(String name);
    
    public File getUploadFile(String name);
    
}
