package org.gogoup.dddutils.remote.impl;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gogoup.dddutils.remote.RemoteHttpMethod;
import org.gogoup.dddutils.remote.RemoteHttpRequest;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

public class DefaultRemoteHttpRequest implements RemoteHttpRequest {
    
    private String protocol;
    private RemoteHttpMethod method;
    private boolean isKeepAlive;
    private String path;
    private String[] headerNames;
    private Map<String, String[]> headers;
    private String[] queryParameterNames;
    private Map<String, String[]> queryParameters;    
    private ChannelBuffer data;
    private ByteBuffer dataBuffer;    
    private String[] attributeNames;
    private Map<String, String> attributes;
    private String[] fileUploadNames;    
    private Map<String, File> uploadFiles;
    private Map<String, String> uploadFileNames;
    
    
    public DefaultRemoteHttpRequest(HttpRequest request) {
        
        this.protocol = readProtocl(request);
        this.method = RemoteHttpMethod.toRemoteHttpMethod(request.getMethod().getName());
        this.isKeepAlive = HttpHeaders.isKeepAlive(request);
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());        
        this.path = queryStringDecoder.getPath();
        this.headerNames = null;
        this.headers = readHeaders(request);
        this.queryParameterNames = null;
        this.queryParameters = readPathAndQueryParameters(queryStringDecoder, request);        
        this.data = null;
        this.dataBuffer = null;        
        this.attributeNames = null;
        this.attributes = new LinkedHashMap<String, String>();        
        this.fileUploadNames = null;        
        this.uploadFiles = new LinkedHashMap<String, File>();
        this.uploadFileNames = new LinkedHashMap<String, String>();
                
    }
    
    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public RemoteHttpMethod getMethod() {
        return method;
    }

    @Override
    public boolean isKeepAlive() {
        return isKeepAlive;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean hasHeader(String name) {
        return headers.containsKey(name);
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
    public boolean hasQueryParameter(String name) {
        return queryParameters.containsKey(name);
    }

    @Override
    public String[] getQueryParameterNames() {
        if (null == queryParameterNames) {
            queryParameterNames = queryParameters.keySet().toArray(new String[queryParameters.size()]);
        }
        return queryParameterNames;
    }

    @Override
    public String[] getQueryParameters(String name) {
        return queryParameters.get(name);
    }

    @Override
    public String getQueryParameter(String name) {
        String[] values = getQueryParameters(name);
        if (null == values || values.length == 0) {
            return null;
        }
        return values[0];
    }

    public void setData(ChannelBuffer data) {
        this.data = data;
        if (null != dataBuffer) {
            dataBuffer.clear();
        }
        dataBuffer = null;
    }

    @Override
    public ByteBuffer getData() {
        if (null == data) {
            return null;
        }
        if (null == dataBuffer) {
            dataBuffer = data.toByteBuffer();
        }
        return dataBuffer;
    }

    @Override
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    public void setAttribute(String name, String value) {
        attributes.put(name, value);
        attributeNames = null;
    }
    
    @Override
    public String[] getAttributeNames() {
        if (null == attributeNames) {
            attributeNames = attributes.keySet().toArray(new String[attributes.size()]);
        }
        return attributeNames;
    }

    @Override
    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public void setUploadFile(String name, String fileName, File file) {        
        uploadFiles.put(name, file);
        uploadFileNames.put(name, fileName);
        fileUploadNames = null;
    }
    @Override
    public String[] getFileUploadNames() {
        if (null == fileUploadNames) {
            fileUploadNames = uploadFiles.keySet().toArray(new String[uploadFiles.size()]);
        }
        return fileUploadNames;
    }

    @Override
    public String getUploadFileNames(String name) {
        return uploadFileNames.get(name);
    }

    @Override
    public File getUploadFile(String name) {
        return uploadFiles.get(name);
    }

    private static String readProtocl(HttpRequest request) {
        HttpVersion version = request.getProtocolVersion();
        StringBuilder protocol = new StringBuilder(version.getProtocolName());
        protocol.append("_");
        protocol.append(version.getMajorVersion());
        protocol.append("_");
        protocol.append(version.getMinorVersion());
        return protocol.toString();
    }

    private static Map<String, String[]> readHeaders(HttpRequest request) {
            
            Map<String, String[]> headers = new LinkedHashMap<String, String[]>();
            
            if (!request.getHeaderNames().isEmpty()) {
                for (String name : request.getHeaderNames()) {
                    List<String> values = request.getHeaders(name);
                    headers.put(name, values.toArray(new String[values.size()]));
    //                System.out.println("HEADER "+name+": [");
    //                for (String v: values) {
    //                    System.out.println(v+", ");
    //                }
    //                System.out.println("];\n");
                }
            }
            
            return headers;
        }

    private static Map<String, String[]> readPathAndQueryParameters(
                QueryStringDecoder queryStringDecoder, HttpRequest request) {
            
            Map<String, String[]> queryParameters = new LinkedHashMap<String, String[]>();
            Map<String, List<String>> parameters = queryStringDecoder.getParameters();
            for (Iterator<String> iter = parameters.keySet().iterator(); iter.hasNext();) {
                String paramName = iter.next();
                List<String> paramValues = parameters.get(paramName);
                queryParameters.put(paramName, paramValues.toArray(new String[paramValues.size()]));
    //            System.out.println("PARAM "+paramName+": [");
    //            for (String v: paramValues) {
    //                System.out.println(v+", ");
    //            }
    //            System.out.println("];\n");
            }
            
            return queryParameters;
        }
}
