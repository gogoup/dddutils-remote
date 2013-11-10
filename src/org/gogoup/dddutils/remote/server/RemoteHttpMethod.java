package org.gogoup.dddutils.remote.server;

public enum RemoteHttpMethod {
	OPTIONS("OPTIONS"),
	GET("GET"),
	HEAD("HEAD"),
	POST("POST"),
	PUT("PUT"),
	DELETE("DELETE"),
	TRACE("TRACE"),
	CONNECT("CONNECT");
	
	private String name;

	private RemoteHttpMethod(String name) {
		this.name=name;
	}
	
	public String getName() {
		return name;		
	}
	
	public static RemoteHttpMethod toRemoteHttpMethod(String name) {
		
		if(name.equalsIgnoreCase("OPTIONS"))
			return OPTIONS;
		else if(name.equalsIgnoreCase("GET"))
			return GET;
		else if(name.equalsIgnoreCase("HEAD"))
			return HEAD;
		else if(name.equalsIgnoreCase("POST"))
			return POST;
		else if(name.equalsIgnoreCase("PUT"))
			return PUT;
		else if(name.equalsIgnoreCase("DELETE"))
			return DELETE;
		else if(name.equalsIgnoreCase("TRACE"))
			return TRACE;
		else if(name.equalsIgnoreCase("CONNECT"))
			return CONNECT;
		
		return null;
	}
}
