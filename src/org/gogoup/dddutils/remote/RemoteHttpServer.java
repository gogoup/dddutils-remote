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

import org.gogoup.dddutils.appsession.AppSessionFactory;

public abstract class RemoteHttpServer {
    
    private RemoteHttpServerConfig config;
    private RemoteHttpServerDelegate delegate;
    
    public RemoteHttpServer(RemoteHttpServerConfig config, RemoteHttpServerDelegate delegate) {
        this.config = config;
        this.setDelegate(delegate);
    }
    
    public RemoteHttpServerConfig getConfig() {
        return this.config;
    }
   
    public void setDelegate(RemoteHttpServerDelegate delegate) {
        this.delegate = delegate;
    }
    
    public RemoteHttpServerDelegate getDelegate() {
        return this.delegate;
    }
    
    public abstract AppSessionFactory getApplication(String applicationId);
    
    public abstract void startup();
    
    public abstract void shutdown();
    
}
