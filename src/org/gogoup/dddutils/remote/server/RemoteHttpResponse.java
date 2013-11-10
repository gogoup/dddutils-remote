package org.gogoup.dddutils.remote.server;

public interface RemoteHttpResponse {

	public void writeResponseMessage(String message);
	
	public void handleException(Throwable exception);
}
