package org.gogoup.dddutils.remote.client;

public interface RemoteConnectionListener {

	public void writeComplete(RemoteConnection connection, boolean isSuccess);
	
	public void readComplete(RemoteConnection connection, boolean isSuccess, Object receivedData);
}
