package org.gogoup.dddutils.remote.client.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.gogoup.dddutils.remote.client.RemoteClient;
import org.gogoup.dddutils.remote.client.RemoteConnection;
import org.gogoup.dddutils.remote.client.RemoteConnectionError;
import org.gogoup.dddutils.remote.client.RemoteConnectionException;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipelineFactory;

public abstract class AbstractNettyRemoteClient implements RemoteClient{	
	
	private static final Logger logger = Logger.getLogger(AbstractNettyRemoteClient.class.getCanonicalName());
	
	//public static final int HTTP_CLIENT=1;
	//public static final int SOCKET_CLIENT=2;
	
	//private final byte[] lock = new byte[0];
	
	private int waiters=0;
	
	//private int type;	
	private String host;
	private int port;
	private NettyClientChannelFactory channelFactory;
	
	private List<NettyRemoteConnection> allConnections;
	private List<NettyRemoteConnection> connectionPool;
	
	protected AbstractNettyRemoteClient(String host, int port, SSLContext sslContext) {
		this(host, port, NettyClientChannelFactory.DEFAULT_INIT_CAPACITY, NettyClientChannelFactory.DEFAULT_MIN_CAPACITY, NettyClientChannelFactory.DEFAULT_MAX_CAPACITY,
				NettyClientChannelFactory.DEFAULT_INCREMENT, sslContext);
    }
	
	protected AbstractNettyRemoteClient(String host, int port, int initCapacity, int minCapacity, int maxCapacity, int increment, SSLContext sslContext) {
		
		//this.type=type;
		allConnections=new LinkedList<NettyRemoteConnection>();
		connectionPool=new LinkedList<NettyRemoteConnection>();		
		this.host=host;
		this.port=port;
		//initial channel factory class.
		this.channelFactory = new NettyClientChannelFactory(
				this.host, 
				this.port, 
				initCapacity, 
				minCapacity, 
				maxCapacity, 
				increment, 
				this.assembblePipeLineFactory(sslContext)); //giving the different pipelie factory
		
    }
	
	protected void initialConnectionPool(int initCapacity) {
		//initial connections.
		NettyRemoteConnection[] initConnections = new NettyRemoteConnection[initCapacity];
		try {
			for(int i=0; i<initCapacity; i++)
			{
				initConnections[i]=(NettyRemoteConnection) this.nextConnection(60000);
			}
			for(int i=0; i<initCapacity; i++)
			{
				this.returnConnection(initConnections[i]);
			}
			
		} catch (RemoteConnectionException e) {
			logger.warning("Fail to initialize Netty remote client connections for the giving capacity due to "+e.getError());
		}
	}
	
	@Override
	public RemoteConnection getConnection() throws RemoteConnectionException {
		
		synchronized(this) {
			
			waiters++;
			RemoteConnection connection = null;
			logger.info("HERE======>getConnection() NEW WAITER COMING IN "+ waiters+ " THREAD: " +Thread.currentThread().getId());
			while(null == connection)
			{
				logger.info("HERE======>getConnection() #1"+ " THREAD: " +Thread.currentThread().getId());
				logger.info("The current number of connections in the pool: "+this.connectionPool.size()+ " THREAD: " +Thread.currentThread().getId());
				
				connection = this.nextConnection(-1);
				if(null!=connection)
				{
					if(connection.isClosed())
					{
						//dispose connection and return included channel.
						this.channelFactory.disposeChannel(((NettyRemoteConnection)connection).getChannel());
						connection=null;
						continue;
					}
					else
					{
						return connection;
					}
				}
				
				try {
					logger.info("HERE======>getConnection() START WAITING"+ " THREAD: " +Thread.currentThread().getId());
					//Thread.sleep(1);
					this.wait();
				} catch (InterruptedException e) {
					logger.warning(e.getMessage());
				}
				logger.info("HERE======>getConnection() END WAITING"+ " THREAD: " +Thread.currentThread().getId());
				
				logger.info("HERE======>getConnection() #2"+ " THREAD: " +Thread.currentThread().getId());
				logger.info("Fetching the next avaiable connection from pool, "+connection+ " THREAD: " +Thread.currentThread().getId());
				
			}
				
			logger.info("HERE======>getConnection() #3 " +connection+ " THREAD: " +Thread.currentThread().getId());
			
			logger.finest("Available Connection; Connection AutoFilledPool Size: "+this.connectionPool.size()+ " THREAD: " +Thread.currentThread().getId());
	 	    return connection;
	 	    
		}

	}
	
	@Override
	public RemoteConnection getConnection(long timeout) throws RemoteConnectionException {
				
		synchronized(this) {
			
			long startTime=timeout<=0?0:System.currentTimeMillis();	
			waiters++;
			RemoteConnection connection = null;
			logger.info("HERE======>getConnection(timeout) NEW WAITER COMING IN "+ waiters+ " THREAD: " +Thread.currentThread().getId());
			while(null == connection)
			{
				logger.info("HERE======>getConnection(timeout) #1"+ " THREAD: " +Thread.currentThread().getId());
				logger.info("The current number of connections in the pool: "+this.connectionPool.size()+" ("+timeout+")"+ " THREAD: " +Thread.currentThread().getId());
				
				connection = this.nextConnection(timeout);
				if(null!=connection)
				{
					if(connection.isClosed())
					{
						logger.info("HERE======>getConnection(timeout) #1.1 CLOSED CONNECTION " +connection+ " THREAD: " +Thread.currentThread().getId());
						//dispose connection and return included channel.
						this.channelFactory.disposeChannel(((NettyRemoteConnection)connection).getChannel());
						connection=null;
						continue;
					}
					else
					{
						logger.info("HERE======>getConnection(timeout) #1.2 CONNECTION " +connection+ " THREAD: " +Thread.currentThread().getId());
						return connection;
					}
				}
				
				try {
					logger.info("HERE======>getConnection(timeout) START WAITING"+ " THREAD: " +Thread.currentThread().getId());
					//Thread.sleep(1);
					this.wait(timeout);
				} catch (InterruptedException e) {
					logger.warning(e.getMessage());
				}
				logger.info("HERE======>getConnection(timeout) END WAITING"+ " THREAD: " +Thread.currentThread().getId());
				if(null==connection 
						&& (System.currentTimeMillis()-startTime)>timeout)
	 	    	{			 		    	    		
					logger.info("HERE======>getConnection(timeout) TIMEOUT"+(System.currentTimeMillis()-startTime)+">"+timeout+"; THREAD: " +Thread.currentThread().getId());
	 	    		throw new RemoteConnectionException("Timeout!", RemoteConnectionError.TIMEOUT);
	 	    	}
				
				logger.info("HERE======>getConnection(timeout) #2"+ " THREAD: " +Thread.currentThread().getId());
				logger.info("Fetching the next avaiable connection from pool, "+connection+ " THREAD: " +Thread.currentThread().getId());
				
			}
				
			logger.info("HERE======>getConnection(timeout) #3 " +connection+ " THREAD: " +Thread.currentThread().getId());
			
			logger.finest("Available Connection; Connection AutoFilledPool Size: "+this.connectionPool.size()+ " THREAD: " +Thread.currentThread().getId());
	 	    return connection;
	 	    
		}
 	    
	}

	@Override
	public void close() {
		
		synchronized(this) {
			
			this.connectionPool.clear();
			
			for (Iterator<NettyRemoteConnection> iter = this.allConnections.iterator(); iter.hasNext();)
			{
				NettyRemoteConnection conn =iter.next();
				conn.close();
			}
			this.allConnections.clear();
			this.channelFactory.close();
		}
		
	}

	public void returnConnection(NettyRemoteConnection connection) {
		
		synchronized(this) {
			
			logger.info("HERE======>returnConnection() #1 "+connection+ " THREAD: " +Thread.currentThread().getId());
			
			logger.info("Connection "+connection+" returned."+ " THREAD: " +Thread.currentThread().getId());
			
			if(connection.isClosed())
			{		
				logger.warning("Connection "+connection+" need to be DISPOSED due to it was disconnected from the remote server."+ " THREAD: " +Thread.currentThread().getId());
				//dispose this connection and return included channel.
				this.channelFactory.disposeChannel(connection.getChannel());
				logger.warning("AFTER CONNECTION "+connection+" DISPOSED "+this.connectionPool.size()+" REMAINS."+" THREAD: " +Thread.currentThread().getId());
			}
			else
			{		
				logger.info("HERE======>returnConnection() #1.2 "+connection+" returned to the connection pool."+ " THREAD: " +Thread.currentThread().getId());
				this.connectionPool.add(connection);
			}	
			
			//logger.info("HERE======>returnConnection() #2 TOTAL: "+this.allConnections.size()+ " THREAD: " +Thread.currentThread().getId());
			
			logger.info("HERE======>returnConnection() #3 REMAIN: "+this.connectionPool.size()+ " THREAD: " +Thread.currentThread().getId());
			/**/
			
			this.notifyAll();
			this.waiters = 0;
			/*
			if(this.waiters>0)
			{
				this.notifyAll();
				this.waiters = 0;
			}*/
			
			logger.info("HERE======>returnConnection() #4 SUCCESS: " + " THREAD: " +Thread.currentThread().getId());
		}
	}
	
	private RemoteConnection nextConnection(long timeout) throws RemoteConnectionException {
		
		//synchronized(lock) {
					
		logger.info("HERE======>nextConnection() #1 REMAIN: "+this.connectionPool.size()+ " THREAD: " +Thread.currentThread().getId());
		
		if(this.connectionPool.size() > 0)
		{
			RemoteConnection connection = this.connectionPool.remove(0);
			logger.info("HERE======>nextConnection() #2.1 CONNECTION: "+connection+ " THREAD: " +Thread.currentThread().getId());
			return connection;
		}
		
		Channel channel=null;			
		logger.info("Fetching channel from the pool."+ " THREAD: " +Thread.currentThread().getId());
		if(timeout > -1)
			channel=this.channelFactory.getChannel(timeout); //need create new channel first.
		else
			channel=this.channelFactory.getChannel();
		logger.info("AFTER Fetching channel from the pool."+ " THREAD: " +Thread.currentThread().getId());
		if(null != channel)
		{
			//there is a new channel returned, then create a new connection and put in the pool.
			logger.info("Will create new connection with channel "+channel+ " THREAD: " +Thread.currentThread().getId()); 	
			//create and cache new connection
			NettyRemoteConnection conn = this.assembleConnection(this, this.host, this.port, channel);
			this.connectionPool.add(conn);
			this.allConnections.add(conn);
			
			logger.info("HERE======>nextConnection() #2.2 "+ " THREAD: " +Thread.currentThread().getId());
			return this.connectionPool.remove(0);
		}
		else
		{
			logger.info("HERE======>nextConnection() #2.3 "+ " THREAD: " +Thread.currentThread().getId());
			return null;
		}
			
			
		//}		
	}
	abstract protected ChannelPipelineFactory assembblePipeLineFactory(SSLContext sslContext);
	abstract protected NettyRemoteConnection assembleConnection(AbstractNettyRemoteClient client, String host, int port, Channel channel);
	
}
