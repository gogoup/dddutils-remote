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

package org.gogoup.dddutils.objectsegment;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBConnectionManager {
	
	private static DBConnectionManager instance=null;
	private static final byte[] lock=new byte[0];
	private Map<String, Object> dataSourceMap;
	
	private DBConnectionManager(){
		dataSourceMap=new HashMap<String, Object>();		
	}
	
	private void initDataSource(String name) {				
		ComboPooledDataSource defaultDS = new ComboPooledDataSource(name);				
		dataSourceMap.put(name, defaultDS);
	}
	
	public static DBConnectionManager getInstance(){
		synchronized(lock){
			if(null==instance)
				instance=new DBConnectionManager();//init
			return instance;
		}
	}
	
	public Object getConnection(String name){
		synchronized(lock) {
			Object conn=null;
			try {
				if(!this.dataSourceMap.containsKey(name))
					this.initDataSource(name);
				
				conn = ((DataSource)dataSourceMap.get(name)).getConnection();				
				
			} catch (SQLException e) {
				
				e.printStackTrace();
			}
			return conn;
		}
	}	
	
	

	/*
	private void initSQLite() {
		File dbFile=new File("resource/bs.sqlite");		
		File inUseDBFile=new File("bs.sqlite");
		if(!inUseDBFile.exists())
		{			
			FileInputStream  in=null;
			FileOutputStream  out=null;
			try {
				in = new FileInputStream(dbFile);
				out = new FileOutputStream(inUseDBFile);
				byte[] buffer = new byte[4096];
				int bytesRead;

				while ((bytesRead = in.read(buffer)) != -1)
					out.write(buffer, 0, bytesRead); // write
			   
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {			
				e.printStackTrace();
			} finally{
				try {
					if(null!=in) in.close();
					if(null!=out) out.close();
				} catch (IOException e) {					
					e.printStackTrace();
				}			
			}
		}		
		try {
			//SQLite Source
			ComboPooledDataSource defaultDS = new ComboPooledDataSource();
			defaultDS.setDriverClass("org.sqlite.JDBC");
			defaultDS.setJdbcUrl("jdbc:sqlite:bs.sqlite");
			//ComboPooledDataSource defaultDS = new ComboPooledDataSource();
			//defaultDS.setDriverClass("org.gjt.mm.mysql.Driver");
			//defaultDS.setJdbcUrl("jdbc:mysql://localhost" );
			//defaultDS.setUser("root");
			//defaultDS.setPassword("123456");
			dataSourceMap.put(AppParameters.SQLITE_DBCONN, defaultDS);
			
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/

}
