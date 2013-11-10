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
