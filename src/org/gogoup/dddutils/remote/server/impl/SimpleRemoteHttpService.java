package org.gogoup.dddutils.remote.server.impl;

import java.nio.ByteBuffer;

import org.gogoup.dddutils.appsession.AppSession;
import org.gogoup.dddutils.appsession.AppSessionContext;
import org.gogoup.dddutils.appsession.InconsistentAppSessionException;
import org.gogoup.dddutils.appsession.NoSuchAppSessionException;
import org.gogoup.dddutils.pool.ParameterResolver;
import org.gogoup.dddutils.remote.server.RemoteHttpResponse;
import org.gogoup.dddutils.remote.server.RemoteHttpService;
import org.gogoup.dddutils.remote.server.SessionParameterNames;
import org.jboss.netty.handler.codec.http.HttpHeaders;

public abstract class SimpleRemoteHttpService implements RemoteHttpService {
	
	private AppSessionContext context;
	private boolean isResolversRegistered;
	
	public SimpleRemoteHttpService() {
		context = null;
		isResolversRegistered = false;
	}

	protected AppSessionContext getContext() {		
		return context;		
	}
	
	protected ParameterResolver[] registerResolvers() {
		return new ParameterResolver[0];
	}
	
	protected String getURLPath() {
		return (String) this.context.getCurrentSession().getParameter(SessionParameterNames.URL_PATH);
	}
	
	protected String[] getQueryParameterNames() {
		return (String[]) this.context.getCurrentSession().getParameter(SessionParameterNames.URL_PARAMETER_NAMES);
	}
	
	protected String[] getHttpHeaderNames() {
		return (String[]) this.context.getCurrentSession().getParameter(SessionParameterNames.HTTP_HEADER_NAMES);
	}
	
	protected long getHttpRequestDataLength() {
		
		String[] values = (String[]) this.context.getCurrentSession().getParameter(HttpHeaders.Names.CONTENT_LENGTH);
		if(null == values) return -1;
		
		String contentLength = values[0];
    	return Long.valueOf(contentLength);
	}
	
	protected ByteBuffer getHttpRequestData() {
		return (ByteBuffer) this.context.getCurrentSession().getParameter(SessionParameterNames.HTTP_REQUEST_DATA);
	}
	
	@Override
	public void init(AppSessionContext context) {

		this.context=context;
		if(!isResolversRegistered)
		{
			ParameterResolver[] resolvers = registerResolvers();
			for(ParameterResolver resolver: resolvers)
			{
				context.registerParameterResolver(resolver);
			}
			isResolversRegistered = true;
		}
		
	}

	@Override
	public void done(AppSession session) {
		
		try {				
			session.sync();

		} catch (InconsistentAppSessionException e1) {
			e1.printStackTrace();
		} catch (NoSuchAppSessionException e1) {
			e1.printStackTrace();
		} finally {
			//close(return) session
			this.context = null;
			session.close();			
		}
	
	}

	@Override
	public void exceptionOccured(Throwable exception, AppSession session,
			RemoteHttpResponse response) {
				
		response.handleException(exception);
		try {
			if(null!=session) session.destory();
		} catch (InconsistentAppSessionException e) {
			e.printStackTrace();
		} catch (NoSuchAppSessionException e) {
			e.printStackTrace();
		} finally {
			//close(return) session.
			this.context = null;					
		}

	}

	@Override
	public void connectionClosed(AppSession session) {
		
		try {
			if(null!=session) session.destory();
		} catch (InconsistentAppSessionException e) {
			e.printStackTrace();
		} catch (NoSuchAppSessionException e) {
			e.printStackTrace();
		} finally {
			//close(return) session.
			this.context = null;					
		}
		
	}

}
