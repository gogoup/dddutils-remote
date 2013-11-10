package org.gogoup.dddutils.appsession.impl;

import java.util.Hashtable;
import java.util.Map;

import org.gogoup.dddutils.appsession.AppService;
import org.gogoup.dddutils.appsession.AppSessionContext;
import org.gogoup.dddutils.appsession.spi.AppSessionOS;
import org.gogoup.dddutils.appsession.spi.AppSessionSPI;

public class InMemoryAppSessionSP implements AppService, AppSessionSPI {

	private static Map<String, AppSessionOS> sessions;
	
	public InMemoryAppSessionSP() {
		sessions = new Hashtable<String, AppSessionOS>();
	}
	
	@Override
	public void init(AppSessionContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasSession(String applicationId, String sessionKey) {
		//System.out.println("HERE======>MemoryAppSessionSP.hasSession() SESSION: "+sessionKey+"; THREAD: "+Thread.currentThread().getId());
		return sessions.containsKey(sessionKey);
	}

	@Override
	public void insert(AppSessionOS os) {
		//System.out.println("HERE======>MemoryAppSessionSP.insert() SESSION: "+ds.getSessionKey()+"; THREAD: "+Thread.currentThread().getId());		
		sessions.put(os.getSessionKey(), os);
		os.cleanMarks();
	}

	@Override
	public void update(AppSessionOS os) {
		//System.out.println("HERE======>MemoryAppSessionSP.update() SESSION: "+ds.getSessionKey()+"; THREAD: "+Thread.currentThread().getId());
		sessions.put(os.getSessionKey(), os);
		os.cleanMarks();
	}

	@Override
	public void delete(AppSessionOS os) {
		//System.out.println("HERE======>MemoryAppSessionSP.delete() SESSION: "+ds.getSessionKey()+"; THREAD: "+Thread.currentThread().getId());
		sessions.remove(os.getSessionKey());
		os.cleanMarks();
	}

	@Override
	public AppSessionOS selectByKey(String applicationId, String sessionKey) {
		//System.out.println("HERE======>MemoryAppSessionSP.selectByKey() SESSION: "+sessionKey+": "+sessions.get(sessionKey)+"; THREAD: "+Thread.currentThread().getId());		
		return sessions.get(sessionKey);
		
	}

}
