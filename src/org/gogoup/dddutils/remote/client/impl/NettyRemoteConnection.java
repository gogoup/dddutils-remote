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

package org.gogoup.dddutils.remote.client.impl;

import org.gogoup.dddutils.remote.client.RemoteConnection;
import org.jboss.netty.channel.Channel;

public abstract class NettyRemoteConnection implements
		RemoteConnection {

	private AbstractNettyRemoteClient client;
	private String host;
	private int port;
	private Channel channel;
	
	public NettyRemoteConnection(AbstractNettyRemoteClient client, String host, int port, Channel channel) {
		this.client=client;
		this.host=host;
		this.port=port;
		this.channel=channel;
	}

	public AbstractNettyRemoteClient getClient() {
		return client;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public Channel getChannel() {
		return channel;
	}


}
