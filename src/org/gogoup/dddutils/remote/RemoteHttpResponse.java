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

package org.gogoup.dddutils.remote;

import java.io.File;
import java.nio.ByteBuffer;

public interface RemoteHttpResponse {
    
    public static final int NONE_CONTENT = 0;
    public static final int MESSAGE_CONTENT = 1;
    public static final int FILE_CONTENT = 2;
    public static final int DATA_CONTENT = 3;
    
    public RemoteHttpRequest getRequest();
    
    public void setProtocol(String protocol);
    
    public String getProtocol();
    
    public void setStatus(int code);
    
    public int getStatus();
    
    public void setHeader(String name, String[] values);
    
    public String[] getHeaderNames();
    
    public String[] getHeaders(String name);
    
    public String getHeader(String name);
    
    public void writeMessage(String message);    
    
    public void writeFile(File file);
    
    public void writeData(ByteBuffer data);
    
    public int getContentCategory();
}
