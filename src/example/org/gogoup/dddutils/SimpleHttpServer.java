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

package example.org.gogoup.dddutils;

import java.util.Map;
import java.util.concurrent.Executors;

import org.gogoup.dddutils.appsession.AppServiceResolver;
import org.gogoup.dddutils.appsession.AppSession;
import org.gogoup.dddutils.appsession.AppSessionContext;
import org.gogoup.dddutils.appsession.AppSessionFactory;
import org.gogoup.dddutils.appsession.AppSessionFactoryDelegate;
import org.gogoup.dddutils.appsession.impl.DefaultAppSession;
import org.gogoup.dddutils.appsession.impl.DefaultAppSessionFactory;
import org.gogoup.dddutils.appsession.impl.InMemoryAppSessionSPResolver;
import org.gogoup.dddutils.appsession.impl.MySQLAppSessionSPResolver;
import org.gogoup.dddutils.misc.CodingHelper;
import org.gogoup.dddutils.objectsegment.DBConnectionManager;
import org.gogoup.dddutils.objectsegment.JDBCResolver;
import org.gogoup.dddutils.pool.ParameterResolver;
import org.gogoup.dddutils.remote.server.RemoteHttpMethod;
import org.gogoup.dddutils.remote.server.RemoteHttpServer;
import org.gogoup.dddutils.remote.server.RemoteHttpServerDelegate;
import org.gogoup.dddutils.remote.server.impl.DefaultHttpServer;

public class SimpleHttpServer {
	
	private static final boolean NEED_SESSION_PERSISTENCE = true; //<--- Setup MySQL Connection before change this to true.
	
	public SimpleHttpServer() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		
		if(args.length < 3)
    	{
    		args=new String[]{
        			"12345",
        			"libs/c3p0-config.xml",
        			"libs/logging.properties"
        	};
    	}
 
	    int port = Integer.parseInt(args[0]);
	    
	    System.setProperty("com.mchange.v2.c3p0.cfg.xml",args[1]);
	    System.setProperty("java.util.logging.config.file", args[2]);
		
		//Create AppSessionFactory
		AppSessionFactory sessionFactory = new DefaultAppSessionFactory(
				"SimpleHttpServer", 
				1,   //min capacity
				100, //max capacity
				10,  //increment
				new AppSessionFactoryDelegate() { //delegate for this factory.

					@Override
					public AppSession assembleAppSession(
							AppSessionFactory factory, String sessionKey) {
						
						return new DefaultAppSession(
								(DefaultAppSessionFactory) factory, sessionKey);
					}

					@Override
					public ParameterResolver[] loadParameterResolvers(
							AppSessionFactory factory, AppSession session) {
						
						if(!NEED_SESSION_PERSISTENCE)
						{
							return new ParameterResolver[] {
									new SimpleParameter()
							};
						}
						
						//for session persistence.
						return new ParameterResolver[] {
								new SimpleParameter(),
								new JDBCResolver(
										DBConnectionManager.getInstance(), 
										new String[]{
											"APP_SESSION_DB", //database name in c3p0 configuration xml file.
											}),
						};
					}

					@Override
					public AppServiceResolver[] loadAppServiceResolvers(
							AppSessionFactory factory, AppSessionContext context) {
						
						if(!NEED_SESSION_PERSISTENCE)
						{
							//install in memory service for session states persistence.
							return new AppServiceResolver[] {
									new SimplServiceResolver(context),
									new InMemoryAppSessionSPResolver(context)
							};
						}
						
						//for session persistence.
						return new AppServiceResolver[] {
								new SimplServiceResolver(context),
								new MySQLAppSessionSPResolver(context) //MySQL session persistence service
						};
					}
			
		});
		
		
		//create http server
		RemoteHttpServer server = new DefaultHttpServer(
				port,  //listening port
				false, //sslEnabled
				sessionFactory, 
				3000,  //readerIdleTime,
				3000,  //writerIdleTime
				0,     //allIdleTime
				Executors.newCachedThreadPool(), //thread pool
				new RemoteHttpServerDelegate() { //server delegate
			
			@Override
			public synchronized String getSessionKey(RemoteHttpMethod method,
					Map<String, String[]> headers,
					Map<String, String[]> queryParameters) {
				
				//Read session id from url parameter
				//if there is no one assigned yet, then generate one for this http request.
				
				String[] sessionKeyParam = queryParameters.get("SessionId");
				String sessionKey = null;
				
				if(null == sessionKeyParam 
						|| sessionKeyParam[0].trim().length() == 0)
				{
					sessionKey =  CodingHelper.nextUUIDString(); //UUID
				}
				else
				{
					sessionKey = sessionKeyParam[0].trim(); 
				}
				return sessionKey;
			}
			
		});
		System.out.println("Starting Server {port: "+port+"}");
		server.startup();
	}
	

}
