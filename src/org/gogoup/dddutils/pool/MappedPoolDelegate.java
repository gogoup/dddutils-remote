package org.gogoup.dddutils.pool;

public interface MappedPoolDelegate<K, T> {

	/**
	 * This method will be called before the giving item returned to the invocation client.
	 * 
	 * If return FALSE, then the item will be disposed.
	 * 
	 * @param pool
	 * @param key
	 * @param item
	 * @return
	 */
	public boolean willBorrow(MappedPool<K, T> pool, K key, T item);
	
	/**
	 * This method will be called before the giving item put back to the pool.
	 * 
	 * If return FALSE, then the item will be disposed.
	 * 
	 * @param pool
	 * @param key
	 * @param item
	 * @return
	 */
	public boolean willReturn(MappedPool<K, T> pool, K key, T item);
	
}
