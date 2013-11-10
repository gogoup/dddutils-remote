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
