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

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gogoup.dddutils.appsession.AppSessionFactory;
import org.gogoup.dddutils.remote.RemoteHttpServer;
import org.gogoup.dddutils.remote.RemoteHttpServerConfig;
import org.gogoup.dddutils.remote.RemoteHttpServerDelegate;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class DefaultHttpServer extends RemoteHttpServer {
    
    private static final ChannelGroup allChannels = new DefaultChannelGroup("DefaultHttpServer");
    
    private Map<String, AppSessionFactory> applications;
    private ChannelFactory factory;
    private ExecutorService sessionExecutorService;    
        
    public DefaultHttpServer(RemoteHttpServerConfig config, RemoteHttpServerDelegate delegate, 
            ExecutorService sessionExecutorPool, Map<String, AppSessionFactory> applications) {
        super(config, delegate);
        this.sessionExecutorService = sessionExecutorPool;
        this.applications = applications;
    }
   
    @Override
    public void startup() {

        // Configure the server.
        factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        
        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new DeafultHttpServerPipelineFactory(this, this.sessionExecutorService));
        
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        // bootstrap.setOption("child.reuseAddress", true);
        // bootstrap.setOption("child.connectTimeoutMillis", 10);
        
        // Bind and start to accept incoming connections.
        Channel channel = bootstrap.bind(new InetSocketAddress(this.getConfig().getPort()));
        
        allChannels.add(channel);
                
    }

    @Override
    public void shutdown() {
        
        ChannelGroupFuture future = allChannels.close();
        future.awaitUninterruptibly();
        factory.releaseExternalResources();
        System.out.println("Complete.");
        System.exit(0);
        
    }

    @Override
    public AppSessionFactory getApplication(String applicationId) {
        return applications.get(applicationId);
    }

    
    
}
