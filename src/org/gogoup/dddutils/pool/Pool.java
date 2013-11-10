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
