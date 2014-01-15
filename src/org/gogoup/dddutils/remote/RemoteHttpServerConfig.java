package org.gogoup.dddutils.remote;

import java.io.File;

public class RemoteHttpServerConfig {
   
    private int port;
    private boolean isSslEnabled;
    private long readerIdleTime;
    private long writerIdleTime;
    private String applicationVender;
    private String applications;
    private int autoDeployDelay; //in seconds
    private long maxPostSize;
    private String sessionIdParameterName;
    private String uploadTemp;
    private long uploadCacheSize;
    private boolean isDebugMode;
    
    public RemoteHttpServerConfig(int port, boolean isSslEnabled, long readerIdleTime, 
            long writerIdleTime, String applicationVender, String applications, int autoDeployDelay,
            long maxPostSize, String sessionIdParameterName, String uploadTemp, long uploadCacheSize,
            boolean isDebugMode) {
        
        this.port = port;
        this.isSslEnabled = isSslEnabled;
        this.readerIdleTime = readerIdleTime;
        this.writerIdleTime = writerIdleTime;
        this.applicationVender = applicationVender;
        this.applications = applications;
        this.autoDeployDelay = autoDeployDelay;
        this.maxPostSize = maxPostSize;
        if (0 == this.maxPostSize)
            throw new IllegalArgumentException("MaxPostSize need to be greater than ZERO (size > 0).");
        this.sessionIdParameterName = sessionIdParameterName;
        if (null == this.sessionIdParameterName || this.sessionIdParameterName.trim().length() == 0)
            throw new IllegalArgumentException("SessionIdParameterName need a non-empty text.");
        File uploadTempFile = new File(uploadTemp);
        if (!uploadTempFile.exists())
            throw new IllegalArgumentException("Upload temp does not exist. "+uploadTemp);
        if (!uploadTempFile.isDirectory())
            throw new IllegalArgumentException("Upload temp is not a directory. "+uploadTemp);
        this.uploadTemp = uploadTemp;
        if (uploadCacheSize <= 0)
            throw new IllegalArgumentException("Upload cache size need to be greater "
                    + "than 0 ("+uploadCacheSize+" > 0)");
        this.uploadCacheSize = uploadCacheSize;
        this.isDebugMode = isDebugMode;
        
    }

    public int getPort() {
        return port;
    }

    public boolean isSslEnabled() {
        return isSslEnabled;
    }

    public long getReaderIdleTime() {
        return readerIdleTime;
    }

    public long getWriterIdleTime() {
        return writerIdleTime;
    }

    public String getApplicationVender() {
        return applicationVender;
    }

    public String getApplications() {
        return applications;
    }

    public int getAutoDeployDelay() {
        return autoDeployDelay;
    }

    public long getMaxPostSize() {
        return maxPostSize;
    }

    public String getSessionIdField() {
        return sessionIdParameterName;
    }

    public String getUploadTemp() {
        return uploadTemp;
    }

    public long getUploadCacheSize() {
        return uploadCacheSize;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

}
