package org.gogoup.dddutils.remote.client;

import java.net.ConnectException;

public interface RemoteConnection {

	public void close();
	
	public boolean isClosed();
	
	public Object sendRequest(Object data);
	
	/**
	 * Sends HTTP request synchronously.
	 * 
	 * @param data
	 * @param timeout
	 * @return
	 * @throws ConnectException TODO
	 * @throws RemoteConnectionException 
	 */
    public Object sendRequest(Object data, long timeout) throws RemoteConnectionException;
    
    public void sendAsynchronousRequest(Object data, RemoteConnectionListener listener);
    
    public void sendRequestAndForget(Object data) throws RemoteConnectionException;
    
    public void sendRequestAndForget(Object data, long timeout) throws RemoteConnectionException;
}
