package org.gogoup.dddutils.objectsegment;

import java.sql.Connection;
import java.sql.SQLException;

import org.gogoup.dddutils.pool.ParameterPool;

public class JDBCResolver extends TransactionalResolver {

	private DBConnectionManager dbConnectionManager;		
	
	public JDBCResolver(DBConnectionManager connManager, String[] connNames){
		super(connNames);
		//initial connection manager
		this.dbConnectionManager=connManager;						
		
	}
	
	@Override
	protected Object startNonTransaction(String id) {
		DBConnectionWrapper connection = new DBConnectionWrapper((Connection) this.dbConnectionManager.getConnection(id), false);			
		return connection;
	}

	@Override
	protected Object startTransaction(String id) {
		
		DBConnectionWrapper connection = new DBConnectionWrapper((Connection) this.dbConnectionManager.getConnection(id), true); 			
		
		try {
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connection;
	}

	@Override
	protected void commitTransaction(String id, Object transaction) {
		
		DBConnectionWrapper connection = (DBConnectionWrapper) transaction;
		
		try {
			if(!connection.isClosed())
			{
				connection.commit();
				connection.close();
			}
			
			
		} catch (SQLException e) {				
			e.printStackTrace();
		} finally {
			
		}
		
	}

	@Override
	protected void rollbackTransaction(String id, Object transaction) {
		
		DBConnectionWrapper connection = (DBConnectionWrapper) transaction;
		try {
			
			if(!connection.isClosed())
			{
				connection.rollback();
				connection.close();
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			
		}
		
	}

	@Override
	public String[] getDependences(ParameterPool pool, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verifyDependence(ParameterPool pool, String name, Object parameter) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] findParameterNames(ParameterPool pool, String query) {		
		return null;
	}

}
