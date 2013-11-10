package org.gogoup.dddutils.remote.server;

import java.util.Map;

public interface RemoteHttpServerDelegate {

//	public RemoteHttpConnectionDelegate getConnectionDelegate();
	
	public String getSessionKey(RemoteHttpMethod method, Map<String, String[]> headers, 
			 Map<String, String[]> queryParameters);
}
