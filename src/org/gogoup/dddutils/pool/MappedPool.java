package org.gogoup.dddutils.pool;

public interface MappedPool<K, T> {

	public void setDelegate(MappedPoolDelegate<K, T> delegate);
	
	public MappedPoolDelegate<K, T> getDelegate();
	
	public int getCapacity(K key);
	
	public int getSize(K key);
	
	public boolean pourItem(K key, T item);
	
	public boolean pullItem(K key, T item);
	
	public void remove(K key);
	
	public T borrowItem(K key);
	
	public T borrowItem(K key, long timeout) throws PoolTimeoutException;
	
	public void returnItem(K key, T item);
}
