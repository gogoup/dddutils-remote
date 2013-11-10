package org.gogoup.dddutils.pool.impl;

import java.util.LinkedList;
import java.util.List;

import org.gogoup.dddutils.pool.Pool;
import org.gogoup.dddutils.pool.PoolDelegate;
import org.gogoup.dddutils.pool.PoolTimeoutException;

/**
 * 
 *           
 *             AutoFilledPool
 * |------------------------------|
 * |   |      |      |            |
 * 0  min    size   capacity    max
 * 
 * 
 */

public abstract class AutoFilledPool<T> implements Pool<T> {
	
	private final static int NO_TIMEOUT = -1;

	public final static int DEFAULT_INIT_CAPACITY = 1;
	public final static int DEFAULT_MAX_CAPACITY = 10;
	public final static int DEFAULT_MIN_CAPACITY = 1;
	public final static int DEFAULT_INCREMENT = 5;
		
	private int minCapacity; //the minimum number of alive channels in the pool.
	private int maxCapacity;
	private int increment;
	private int capacity; //current capacity of pool (need to be greater equal than minimum capacity and less equal than maximum capacity)
	private List<T> pool;
	private List<T> borrowedItems;
	private PoolDelegate<T> delegate;
	private int waitingThreads=0;
	
	public AutoFilledPool(int minCapacity, int maxCapacity, int increment, PoolDelegate<T> delegate) {
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
		this.capacity=0;
		this.pool=new LinkedList<T>();
		this.borrowedItems=new LinkedList<T>();
		this.setDelegate(delegate);
	}
	
	public AutoFilledPool(int initCapacity, int minCapacity, int maxCapacity, int increment, long timeout, PoolDelegate<T> delegate) throws PoolTimeoutException {
		this(minCapacity, maxCapacity, increment, delegate);
		
		if(initCapacity > maxCapacity)
			throw new IllegalArgumentException("Initial capacity cannot exceed max capacity, ("+initCapacity+" > "+maxCapacity+").");
		if(initCapacity < minCapacity)
			throw new IllegalArgumentException("Initial capacity cannot be less than min capacity, ("+initCapacity+" < "+minCapacity+").");
				
		this.fillPool(initCapacity, timeout);
	}
	
	@Override
	public void setDelegate(PoolDelegate<T> delegate) {
		this.delegate=delegate;
	}

	@Override
	public PoolDelegate<T> getDelegate() {
		return delegate;
	}

	@Override
	public int getCapacity() {
		synchronized(this) {
			return this.capacity;
		}
	}

	@Override
	public int getSize() {
		synchronized(this) {
			return this.pool.size();
		}
	}

	@Override
	public boolean addItem(T item) {
		
		synchronized(this) {
			
			if(this.isFull()) return false;

			this.pool.add(item);
			this.capacity++;
			
			this.wakeUp();
			
			return true;
		}
		
	}

	@Override
	public boolean removeItem(T item) {
		
		synchronized(this) {
			
			if(!this.borrowedItems.contains(item)) return false;

			this.borrowedItems.remove(item);
			this.capacity--;
			
			this.wakeUp();
			
			return true;
		}
		
		
	}

	@Override
	public T borrowItem() {
		
		synchronized(this) {
			this.waitingThreads++;
			//System.out.println("HERE======>AutoFilledPool.borrowItem() #1 WAITING THREAD: "+this.waitingThreads+"; THREAD: "+Thread.currentThread().getId());
			//System.out.println("======");
			try {
				while(true)
				{
					//System.out.println("HERE======>borrowItem()#1 "+"; THREAD: "+Thread.currentThread().getId());
					T item = this.nextItem(NO_TIMEOUT);
					if(null != item)
					{
						//System.out.println("HERE======>borrowItem()#1.1 "+"; THREAD: "+Thread.currentThread().getId());
						if(null == this.delegate
								|| this.delegate.willBorrow(this, item))
						{
							borrowedItems.add(item);
							this.waitingThreads--;
							//System.out.println("HERE======>AutoFilledPool.borrowItem() #1.1.1 ITEM: "+item+"; THREAD: "+Thread.currentThread().getId());
							//System.out.println("BORROW ITEM: "+this.getSize()+" / "+this.getCapacity()+" ITEMS LEFT"+"; THREAD: "+Thread.currentThread().getId());
							return item;							
						}
						//System.out.println("HERE======>borrowItem()#1.2 "+"; THREAD: "+Thread.currentThread().getId());
						//this.capacity--;//shrink pool (Unnecessary!) 
						item = null;
						continue;
					}
					//System.out.println("HERE======>borrowItem()#2 "+"; THREAD: "+Thread.currentThread().getId());
					this.wait();					
					//System.out.println("HERE======>borrowItem()#3 "+"; THREAD: "+Thread.currentThread().getId());
				}
			} catch (PoolTimeoutException e1) {					
				e1.printStackTrace();
			} catch (InterruptedException e) {			
				e.printStackTrace();
			}
			
			return null;
		}
	}
	
	@Override
	public T borrowItem(long timeout) throws PoolTimeoutException {
		
		synchronized(this) {
			
			long startTime=timeout<=0?0:System.currentTimeMillis();	
			waitingThreads++;
						
			try {			
				while(true)
				{					
					T item = this.nextItem(timeout);
					if(null != item)
					{
						if(null == this.delegate
								|| this.delegate.willBorrow(this, item))
						{
							borrowedItems.add(item);
							this.waitingThreads--;
							return item;							
						}
						//System.out.println("HERE======>borrowItem()#1.2 "+"; THREAD: "+Thread.currentThread().getId());
						this.capacity--;//shrink pool
						item = null;
						continue;
					}
	
					this.wait(timeout);
									
					if((System.currentTimeMillis()-startTime)>timeout)
		 	    	{			 		    	    							
		 	    		throw new PoolTimeoutException("");
		 	    	}				
				}
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
			
			return null;
		}
	}
	
	@Override
	public void returnItem(T item) {		

		synchronized(this) {			
			
			if(!this.borrowedItems.contains(item)) return;
			
			if(null == this.delegate
					|| this.delegate.willReturn(this, item))
			{				
				this.pool.add(item);
				//System.out.println("RETURN ITEM: "+this.getSize()+" / "+this.getCapacity()+" ITEMS LEFT"+"; THREAD: "+Thread.currentThread().getId());
			}
			else
			{
				//this.capacity--;//shrink pool(Unnecessary!) 
			}
			
			this.borrowedItems.remove(item);
			//System.out.println("HERE======>return()#1 WAITING THREAD HAS: "+this.waitingThreads+"; THREAD: "+Thread.currentThread().getId());
			//System.out.println("======");
			this.wakeUp();
			
			//System.out.println("HERE======>return()#2 WAITING THREAD HAS: "+this.waitingThreads+"; THREAD: "+Thread.currentThread().getId());
			//System.out.println("======");
		}
	}
	
	private boolean isFull() {
		return this.capacity >= this.maxCapacity;
	}

	/**
	 * Calculates the increment based on the current number of items in the pool and current capacity of the pool.
	 * 
	 * @return
	 */
	private int calculateIncrement() {
		
		//System.out.println("HERE======>AutoFilledPool.calculateIncrement() MIN: "+this.minCapacity+"; MAX: "+this.maxCapacity+"; THREAD: "+Thread.currentThread().getId());
		//System.out.println("HERE======>AutoFilledPool.calculateIncrement() SIZE: "+this.pool.size()+"; CAPACITY: "+this.capacity+"; THREAD: "+Thread.currentThread().getId());
		//System.out.println("HERE======>AutoFilledPool.calculateIncrement() IS FULL: "+this.isFull()+"; THREAD: "+Thread.currentThread().getId());
		
		int nextIncrement = 0;
		//check if the current number of items in the pool is lower than the minimum capacity requirement.
		//and the current capacity has not reach the maximum capacity limitation yet.
		if((this.pool.size() < this.minCapacity
				|| this.pool.size() == 0)
				&& !this.isFull())
		{						
			nextIncrement = this.increment;
			int newCapacity = this.capacity + nextIncrement;
			//System.out.println("HERE======>AutoFilledPool.calculateIncrement() NEW CAPACITY: "+newCapacity+"; INCREMENT: "+nextIncrement+"; THREAD: "+Thread.currentThread().getId());
			//Check if the pool is overflow.
			if(newCapacity > this.maxCapacity)
				nextIncrement = this.maxCapacity - this.capacity;
		}
		//System.out.println("HERE======>AutoFilledPool.calculateIncrement() INCREMENT: "+nextIncrement+"; THREAD: "+Thread.currentThread().getId());
		return nextIncrement;
	}
	
	private void fillPool(int increment, long timeout) throws PoolTimeoutException {
		//System.out.println("HERE======>AutoFilledPool.fillPool() INCREMENT: "+increment);
		
		int counts = 0;
		while(counts<increment)
		{
			T item = null;
			if(timeout == NO_TIMEOUT)
				item = this.fill();
			else
				item = this.fill(timeout);
			
			this.pool.add(item);
			counts++;
		}
		this.capacity += counts;
		//System.out.println("HERE======>AutoFilledPool.fillPool() CAPACITY: "+this.capacity+"; THREAD: "+Thread.currentThread().getId());
	}
	
	private T nextItem(long timeout) throws PoolTimeoutException {
		
		int increment = this.calculateIncrement();		
		this.fillPool(increment, timeout);		
		if(this.pool.size() > 0) return this.pool.remove(0);		
		return null;
	}
	
	private void wakeUp() {
		//System.out.println("HERE======>AutoFilledPool.wakeUp() #1 WAITING THREAD: "+this.waitingThreads+"; THREAD: "+Thread.currentThread().getId());
		if(this.waitingThreads>0)
		{
			//System.out.println("HERE======>AutoFilledPool.wakeUp() #1.1"+"; THREAD: "+Thread.currentThread().getId());
			this.notify();
			//this.waitingThreads--;
		}
		//System.out.println("HERE======>AutoFilledPool.wakeUp() #2 WAITING THREAD: "+this.waitingThreads+"; THREAD: "+Thread.currentThread().getId());
	}
	
	abstract protected T fill();
	
	abstract protected T fill(long timeout) throws PoolTimeoutException;
}
