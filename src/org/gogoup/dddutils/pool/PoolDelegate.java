package org.gogoup.dddutils.pool;

public interface PoolDelegate<T> {
	
	/**
	 * This method will be called before the giving item returned to the invocation client.
	 * 
	 * If return FALSE, then the item will be disposed.
	 * 
	 * @param pool
	 * @param item
	 * @return
	 */
	public boolean willBorrow(Pool<T> pool, T item);
	
	/**
	 * This method will be called before the giving item put back to the pool.
	 * 
	 * If return FALSE, then the item will be disposed.
	 * 
	 * @param pool
	 * @param item
	 * @return
	 */
	public boolean willReturn(Pool<T> pool, T item);
	
}
