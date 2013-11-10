package org.gogoup.dddutils.pool;

/**
 * This interface specifies the behaviours of a Pool. 
 * 
 * 
 * |----------Pool-------------|
 * |<---------capacity-------->|
 * |<----size--->|
 * 
 * @author ruisun
 *
 * @param <T>
 */
public interface Pool<T> {
	
	/**
	 * Assign a delegate to the pool. 
	 * 
	 * @param delegate PoolDelegate<T>
	 */
	public void setDelegate(PoolDelegate<T> delegate);
	
	/**
	 * Return a delegate.
	 * 
	 * @return PoolDelegate<T>
	 */
	public PoolDelegate<T> getDelegate();

	/**
	 * Return the capacity of the pool.
	 * 
	 * @return
	 */
	public int getCapacity();
	
	/**
	 * Return the actual usage of the pool.
	 * 
	 * @return
	 */
	public int getSize();
	
	/**
	 * Add a new item into the pool.
	 * 
	 * This will cause the capacity of the pool increase.
	 * 
	 * @param item T 
	 * @return boolean Return true if the item add to the pool success, otherwise return false.
	 */
	public boolean addItem(T item);
	
	/**
	 * Remove the giving item from the pool.
	 * 
	 * @param item
	 * @return
	 */
	public boolean removeItem(T item);
	
	/**
	 * Fetch an item from pool. 
	 * 
	 * @return
	 */
	public T borrowItem();
	
	/**
	 * Fetch an item from pool in the specified time period.
	 * 
	 * 
	 * 
	 * @param timeout long
	 * @return T
	 * @throws PoolTimeoutException If no item returned in time.
	 */
	public T borrowItem(long timeout) throws PoolTimeoutException;
	
	/**
	 * Put the borrowed item back to pool. 
	 * 
	 * @param item T
	 */
	public void returnItem(T item);
}
