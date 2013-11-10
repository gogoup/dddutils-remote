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
