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

public interface MappedPoolDelegate<K, T> {
    
    /**
     * This method will be called before the giving item returned to the
     * invocation client.
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
