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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;

import org.gogoup.dddutils.misc.RemoteAPISecurityHelper;
import org.gogoup.dddutils.remote.RemoteHttpServer;
import org.gogoup.dddutils.remote.RemoteHttpServerConfig;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import org.jboss.netty.handler.codec.http.multipart.DiskFileUpload;
import org.jboss.netty.handler.codec.http.multipart.HttpDataFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;

public class DeafultHttpServerPipelineFactory implements ChannelPipelineFactory {
    
    private final ChannelHandler idleStateHandler;
    private RemoteHttpServer server;
    private RemoteHttpServerConfig config;
    private ExecutorService sessionExecutorService;
    private HttpDataFactory httpDataFactory;
    
    public DeafultHttpServerPipelineFactory(RemoteHttpServer server, ExecutorService sessionExecutorService) {
        
        this.server = server;
        this.config = this.server.getConfig();
        this.sessionExecutorService = sessionExecutorService;
        
        this.idleStateHandler = new IdleStateHandler(new HashedWheelTimer(), 
                config.getReaderIdleTime(), config.getWriterIdleTime(), 0, TimeUnit.SECONDS);
        
        DiskFileUpload.deleteOnExitTemporaryFile = true;
        DiskFileUpload.baseDirectory = this.config.getUploadTemp();
        this.httpDataFactory = new DefaultHttpDataFactory(this.config.getUploadCacheSize());
    }
    
    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = null;
        if (null != this.idleStateHandler) {
            pipeline = Channels.pipeline(this.idleStateHandler);
        } else
            pipeline = Channels.pipeline();
        
        // Uncomment the following line if you want HTTPS
        if (this.config.isSslEnabled()) {
            SSLEngine engine = RemoteAPISecurityHelper.getServerSSLContext().createSSLEngine();
            engine.setUseClientMode(false);
            SslHandler sslHandler = new SslHandler(engine);
            sslHandler.setIssueHandshake(false);
            pipeline.addLast("ssl", new SslHandler(engine));
        }
        
        pipeline.addLast("decoder", new HttpRequestDecoder());
        // Uncomment the following line if you don't want to handle HttpChunks.
        // pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
        // 10MB
        pipeline.addLast("encoder", new HttpResponseEncoder());
        // Remove the following line if you don't want automatic content
        // compression.
        pipeline.addLast("deflater", new HttpContentCompressor());
        pipeline.addLast("handler", this.getHandler());
        return pipeline;
    }
    
    private DefaultHttpChannelHandler getHandler() {       
        return new DefaultHttpChannelHandler(this.server, this.sessionExecutorService,
                this.httpDataFactory);
    }
    
}
