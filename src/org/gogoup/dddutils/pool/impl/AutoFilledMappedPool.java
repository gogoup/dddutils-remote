package org.gogoup.dddutils.pool.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gogoup.dddutils.pool.MappedPool;
import org.gogoup.dddutils.pool.MappedPoolDelegate;
import org.gogoup.dddutils.pool.Pool;
import org.gogoup.dddutils.pool.PoolDelegate;
import org.gogoup.dddutils.pool.PoolTimeoutException;

public abstract class AutoFilledMappedPool<K, T> implements MappedPool<K, T> {

	private int minCapacity; //the minimum number of alive channels in the pool.
	private int maxCapacity;
	private int increment;
	private Map<K, MappablePool<K, T>> pools;	
	private MappedPoolDelegate<K, T> delegate;	
	
	public AutoFilledMappedPool(int minCapacity, int maxCapacity, 
			int increment, MappedPoolDelegate<K, T> delegate) {
		
		if(maxCapacity == 0)
			throw new IllegalArgumentException("Max capacity need to be greater than 0.");
		if(increment <= 0)
			throw new IllegalArgumentException("Increment need to be greater than 0.");		
		if(increment > maxCapacity)
			throw new IllegalArgumentException("Increment cannot exceed max capacity, ("+increment+" > "+maxCapacity+").");
		if(minCapacity > maxCapacity)
			throw new IllegalArgumentException("Min capacity cannot exceed max capacity, ("+minCapacity+" > "+maxCapacity+").");
				
		this.minCapacity=minCapacity;
		this.maxCapacity=maxCapacity;
		this.increment=increment;
		this.pools=new LinkedHashMap<K, MappablePool<K, T>>(this.maxCapacity);		
		this.setDelegate(delegate);
	}
	
	@Override
	public void setDelegate(MappedPoolDelegate<K, T> delegate) {
		this.delegate=delegate;
	}

	@Override
	public MappedPoolDelegate<K, T> getDelegate() {
		return delegate;
	}

	@Override
	public int getCapacity(K key) {
		return this.pools.get(key).getCapacity();
	}

	@Override
	public int getSize(K key) {
		return this.pools.get(key).getSize();
	}
	
	@Override
	public boolean pourItem(K key, T item) {
		
		MappablePool<K, T> pool = getPoolWithoutNull(key);
		return pool.addItem(item);
	}

	@Override
	public boolean pullItem(K key, T item) {
		MappablePool<K, T> pool = this.getPool(key);
		return null == pool?false:pool.removeItem(item);
	}

	@Override
	public void remove(K key) {
		synchronized(this.pools) {
			this.pools.remove(key);
		}
	}

	@Override
	public T borrowItem(K key) {
		//System.out.println("HERE======>AutoFilledMappedPool.borrowItem() #1 KEY: "+key+"; THREAD: "+Thread.currentThread().getId());
		MappablePool<K, T> pool = getPoolWithoutNull(key);
		//System.out.println("HERE======>AutoFilledMappedPool.borrowItem() #2 POOL: "+pool+"; THREAD: "+Thread.currentThread().getId());
		T item = pool.borrowItem();
		//System.out.println("HERE======>AutoFilledMappedPool.borrowItem() #3 ITEM: "+item+"; THREAD: "+Thread.currentThread().getId());
		return item;
	}

	@Override
	public T borrowItem(K key, long timeout) throws PoolTimeoutException {
		MappablePool<K, T> pool = getPoolWithoutNull(key);
		return pool.borrowItem(timeout);
	}

	@Override
	public void returnItem(K key, T item) {
		//System.out.println("HERE======>AutoFilledMappedPool.returnItem() #1 KEY: "+key+"; THREAD: "+Thread.currentThread().getId());
		MappablePool<K, T> pool = this.getPool(key);
		//System.out.println("HERE======>AutoFilledMappedPool.returnItem() #2 KEY: "+key+"; THREAD: "+Thread.currentThread().getId());
		if(null != pool) pool.returnItem(item);	
		//System.out.println("HERE======>AutoFilledMappedPool.returnItem() #3 KEY: "+key+"; THREAD: "+Thread.currentThread().getId());
		
	}	
	
	private MappablePool<K, T> getPoolWithoutNull(K key) {
		synchronized(this) {
			//System.out.println("HERE======>AutoFilledMappedPool.getPoolWithoutNull() #1 KEY: "+key+"; THREAD: "+Thread.currentThread().getId());
			MappablePool<K, T> pool = this.pools.get(key);
			if(null == pool)
			{
				MappablePoolDelegate<K, T> poolDelegate = new MappablePoolDelegate<K, T>(key, this);
				pool = new MappablePool<K, T>(key, this, this.minCapacity, this.maxCapacity, this.increment, poolDelegate);
				this.pools.put(key, pool);
			}
			//System.out.println("HERE======>AutoFilledMappedPool.getPoolWithoutNull() #2 KEY: "+key+"; THREAD: "+Thread.currentThread().getId());
			return pool;
		}
	}
	
	private MappablePool<K, T> getPool(K key) {
		synchronized(this) {
			return this.pools.get(key);
		}
	}

	abstract protected T fill(K key);
	
	abstract protected T fill(K key, long timeout) throws PoolTimeoutException;

	private static class MappablePool<K, T> extends AutoFilledPool<T> {
				
		private K key;
		private AutoFilledMappedPool<K,T> pool;
		
		public MappablePool(K key, AutoFilledMappedPool<K,T> pool, int minCapacity, int maxCapacity,
				int increment, PoolDelegate<T> delegate) {
			super(minCapacity, maxCapacity, increment, delegate);
			
			this.key=key;
			this.pool=pool;
		}

		@Override
		protected T fill() {
			return this.pool.fill(this.key);
		}

		@Override
		protected T fill(long timeout) throws PoolTimeoutException {
			return this.pool.fill(this.key, timeout);
		}
		
	}
	
	private static class MappablePoolDelegate<K, T> implements PoolDelegate<T> {
		
		private K key;
		private AutoFilledMappedPool<K,T> pool;
		
		public MappablePoolDelegate(K key, AutoFilledMappedPool<K,T> pool) {
			this.key=key;
			this.pool=pool;
		}

		@Override
		public boolean willBorrow(Pool<T> pool, T item) {
			if(null == this.pool.getDelegate()) return true;
			return this.pool.getDelegate().willBorrow(this.pool, this.key, item);
		}

		@Override
		public boolean willReturn(Pool<T> pool, T item) {
			if(null == this.pool.getDelegate()) return true;
			return this.pool.getDelegate().willReturn(this.pool, this.key, item);
		}
		
	}
}
