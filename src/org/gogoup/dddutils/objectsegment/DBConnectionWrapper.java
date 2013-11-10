package org.gogoup.dddutils.objectsegment;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

public class DBConnectionWrapper implements Connection{

	private Connection helper;
	private boolean isTransaction;	
	
	public DBConnectionWrapper(Connection conn, boolean isTransaction){
		helper=conn;		
		this.isTransaction=isTransaction;		
	}
	
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return helper.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return helper.isWrapperFor(iface);
	}

	@Override
	public Statement createStatement() throws SQLException {
		return helper.createStatement();
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return helper.prepareStatement(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return helper.prepareCall(sql);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		return helper.nativeSQL(sql);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		helper.setAutoCommit(autoCommit);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return helper.getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		helper.commit();
		this.isTransaction = false;
	}

	@Override
	public void rollback() throws SQLException {
		helper.rollback();
		this.isTransaction = false;
	}

	@Override
	public void close() throws SQLException {
		if(!isTransaction)	helper.close();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return helper.isClosed();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return helper.getMetaData();
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		helper.setReadOnly(readOnly);
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return helper.isReadOnly();
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		helper.setCatalog(catalog);
	}

	@Override
	public String getCatalog() throws SQLException {
		return helper.getCatalog();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		helper.setTransactionIsolation(level);
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return helper.getTransactionIsolation();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return helper.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		helper.clearWarnings();
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return helper.createStatement(resultSetType, resultSetConcurrency);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return helper.prepareStatement(sql, resultSetType,resultSetConcurrency);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return helper.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return helper.getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		helper.setTypeMap(map);
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		helper.setHoldability(holdability);
	}

	@Override
	public int getHoldability() throws SQLException {
		return helper.getHoldability();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return helper.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return helper.setSavepoint();
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		helper.rollback(savepoint);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		helper.releaseSavepoint(savepoint);
	}

	@Override
	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return helper.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return helper.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return helper.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		return helper.prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		return helper.prepareStatement(sql, columnIndexes);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		return helper.prepareStatement(sql, columnNames);
	}

	@Override
	public Clob createClob() throws SQLException {
		return helper.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return helper.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return helper.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return helper.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return helper.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		helper.setClientInfo(name, value);
	}

	@Override
	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		helper.setClientInfo(properties);
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return helper.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return helper.getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return helper.createArrayOf(typeName, elements);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		return helper.createStruct(typeName, attributes);
	}

}
