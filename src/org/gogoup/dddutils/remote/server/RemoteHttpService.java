package org.gogoup.dddutils.remote.server;

import org.gogoup.dddutils.appsession.AppService;
import org.gogoup.dddutils.appsession.AppSession;

public interface RemoteHttpService extends AppService{

	public void process(RemoteHttpMethod method, AppSession session, RemoteHttpResponse response);
	
	public void done(AppSession session);
	
	public void exceptionOccured(Throwable exception, AppSession session, RemoteHttpResponse response);
    
	public void connectionClosed(AppSession session);
}
