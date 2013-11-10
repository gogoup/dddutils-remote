package org.gogoup.dddutils.remote.client;

public class RemoteConnectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4057866821477724907L;

	private RemoteConnectionError error;
	
	public RemoteConnectionException(String msg, RemoteConnectionError error) {
		super(msg);
		this.error = error;
	}

	public RemoteConnectionError getError() {
		return error;
	}
	
	
	
}
