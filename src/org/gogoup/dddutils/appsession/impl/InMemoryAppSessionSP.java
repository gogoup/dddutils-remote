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
