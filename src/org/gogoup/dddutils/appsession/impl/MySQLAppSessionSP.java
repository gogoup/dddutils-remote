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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.gogoup.dddutils.appsession.AppService;
import org.gogoup.dddutils.appsession.AppSessionContext;
import org.gogoup.dddutils.appsession.spi.AppSessionOS;
import org.gogoup.dddutils.appsession.spi.AppSessionSPI;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MySQLAppSessionSP implements AppService, AppSessionSPI {
	
	public static final String DB_CONN_NAME = "APP_SESSION_DB";
	
	private static final String CHECK_HAS_SESSION="SELECT COUNT(*) FROM appsession.session WHERE ApplicationId=? AND SessionKey=? LIMIT 1;";
	
	private static final String INSERT_SESSION="INSERT INTO appsession.session (ApplicationId, SessionKey, LastUpdateTime, States) VALUES (?, ?, ?, ?);";	
	private static final String UPDATE_SESSION="UPDATE appsession.session SET LastUpdateTime=?, States=? WHERE ApplicationId=? AND SessionKey=?; ";
	private static final String DELETE_SESSION="DELETE FROM appsession.session WHERE ApplicationId=? AND SessionKey=?; ";	
	private static final String SELECT_SESSION_BY_KEY="SELECT * FROM appsession.session WHERE ApplicationId=? AND SessionKey=? ;";
	
	private AppSessionContext context;
	
	public MySQLAppSessionSP() {

	}
	
	private AppSessionContext getSessionContext() {
		return this.context;
	}

	@Override
	public void init(AppSessionContext context) {
		this.context=context;
	}

	@Override
	public boolean hasSession(String applicationId, String sessionKey) {
		int counter=0;				
		Connection conn=null;
		PreparedStatement stmt=null;
		try {			
			conn=(Connection) this.getSessionContext().getCurrentSession().getParameter(MySQLAppSessionSP.DB_CONN_NAME);
			stmt=conn.prepareStatement(CHECK_HAS_SESSION);
			stmt.setString(1, applicationId);
			stmt.setString(2, sessionKey);
			
			ResultSet rs=stmt.executeQuery();
			
			if (rs.next()) counter=rs.getInt(1);			
			
		} catch (SQLException e) {			
			//e.printStackTrace();
		} finally {
			
			try {
				if(null!=stmt) stmt.close();
				if(null!=conn) conn.close();
			} catch (SQLException e) {				
				e.printStackTrace();
			}			
		}		
		return counter>0?true:false;
	}

	@Override
	public void insert(AppSessionOS os) {
		
		Connection conn=null;
		PreparedStatement stmt=null;
		try {			
			conn=(Connection) this.getSessionContext().getCurrentSession().getParameter(MySQLAppSessionSP.DB_CONN_NAME);
			stmt=conn.prepareStatement(INSERT_SESSION);
			
			stmt.setString(1, os.getApplicationId());
			stmt.setString(2, os.getSessionKey());						
			stmt.setLong(3, os.getLastUpdateTime().getTime());	
			String json = new Gson().toJson(os.getStates(), new TypeToken<Map<String, Object>>() {}.getType());			
			stmt.setString(4, json);
	
			stmt.executeUpdate();					
			
		} catch (SQLException e) {			
			e.printStackTrace();
		} finally {
			try {
				if(null!=stmt) stmt.close();
				if(null!=conn) conn.close();
			} catch (SQLException e) {			
				e.printStackTrace();
			}			
		}
	}

	@Override
	public void update(AppSessionOS os) {

		Connection conn=null;
		PreparedStatement stmt=null;
		try {			
			conn=(Connection) this.getSessionContext().getCurrentSession().getParameter(MySQLAppSessionSP.DB_CONN_NAME);
			stmt=conn.prepareStatement(UPDATE_SESSION);
			stmt.setLong(1, os.getModificationTime().getTime());
			String json = new Gson().toJson(os.getStates(), new TypeToken<Map<String, Object>>() {}.getType());			
			stmt.setString(2, json);
			stmt.setString(3, os.getApplicationId());
			stmt.setString(4, os.getSessionKey());	
			
			stmt.executeUpdate();		
			
		} catch (SQLException e) {			
			//e.printStackTrace();
		} finally {
			try {
				if(null!=stmt) stmt.close();
				if(null!=conn) conn.close();
			} catch (SQLException e) {			
				//e.printStackTrace();
			}			
		}
	}

	@Override
	public void delete(AppSessionOS os) {
		
		Connection conn=null;
		PreparedStatement stmt=null;
		try {			
			conn=(Connection) this.getSessionContext().getCurrentSession().getParameter(MySQLAppSessionSP.DB_CONN_NAME);
			stmt=conn.prepareStatement(DELETE_SESSION);
			stmt.setString(1, os.getApplicationId());
			stmt.setString(2, os.getSessionKey());	
			
			stmt.executeUpdate();			
		} catch (SQLException e) {			
			//e.printStackTrace();
		} finally {
			
			try {
				if(null!=stmt) stmt.close();
				if(null!=conn) conn.close();
			} catch (SQLException e) {				
				e.printStackTrace();
			}
		}
	}

	@Override
	public AppSessionOS selectByKey(String applicationId, String sessionKey) {
		
		AppSessionOS os = null;
		
		Connection conn=null;
		PreparedStatement stmt=null;
		try {			
			
			conn=(Connection) this.getSessionContext().getCurrentSession().getParameter(MySQLAppSessionSP.DB_CONN_NAME);
			stmt=conn.prepareStatement(SELECT_SESSION_BY_KEY);
			stmt.setString(1, applicationId);
			stmt.setString(2, sessionKey);			
			
			ResultSet rs=stmt.executeQuery();
			
			if (rs.next())
			{
				os = assembleSession(rs);
			}
			
			stmt.close();
			conn.close();
			
		} catch (SQLException e) {			
			e.printStackTrace();
		} finally {
			
			try {
				if(null!=stmt) stmt.close();
				if(null!=conn) conn.close();
			} catch (SQLException e) {				
				e.printStackTrace();
			}
		}
		
		return os;
		
	}
	
	private static AppSessionOS assembleSession(ResultSet rs) throws SQLException {
		
		Map<String, Object> states = new Gson().fromJson(rs.getString("States"), new TypeToken<Map<String, Object>>() {}.getType());
		
		return new AppSessionOS(
				rs.getString("ApplicationId"),
				rs.getString("SessionKey"),			
				states,
				new Date(rs.getLong("LastUpdateTime"))				
				);
		
	}

}
