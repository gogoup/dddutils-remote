package org.gogoup.dddutils.remote.client;


public interface RemoteClient {

	/**
	 * Return a connection.
	 * 
	 * To make sure the connectivity for the returned connection before using it.
	 * 
	 * RemoteConnection conn = remoteClient.getConnection();
	 * if(conn.isClosed) conn.close(); //close disconnected connection to make sure it can be handled by the pool properly.
	 * 
	 * ... to do something ...
	 * 
	 * 
	 * @return
	 * @throws RemoteConnectionException TODO
	 */
	public RemoteConnection getConnection() throws RemoteConnectionException;
	
	/**
	 * Return a connection before the giving time.
	 * 
	 * @param timeout
	 * @return
	 * @throws RemoteConnectionException TODO
	 */
	public RemoteConnection getConnection(long timeout) throws RemoteConnectionException;
	
	/**
	 * Close and release all connections.
	 * 
	 */
	public void close();
}
