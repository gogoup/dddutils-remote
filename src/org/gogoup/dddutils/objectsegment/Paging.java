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

package org.gogoup.dddutils.objectsegment;

import org.gogoup.dddutils.misc.ProgrammingLogicException;

public class Paging {
    
    private int pageStart;
    private int pageSize;
    private Ordering[] orderings;
    
    public Paging(int pageStart, int pageSize) {
        this(pageStart, pageSize, new Ordering[0]);
    }
    
    public Paging(int pageStart, int pageSize, Ordering[] orderings) {
        
        if (pageStart < 1)
            throw new ProgrammingLogicException("Invalid page start value, >=1", null);
        if (pageSize < 1 && pageSize != -1)
            throw new ProgrammingLogicException("Invalid page size value, >=1 or -1", null);
        
        this.pageStart = pageStart;
        this.pageSize = pageSize;
        this.orderings = orderings;
    }
    
    public int getPageStart() {
        return pageStart;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public Ordering[] getOrderings() {
        return orderings;
    }
    
    public static Paging paging(int pageStart, int pageSize) {
        return new Paging(pageStart, pageSize);
    }
    
    public static Paging paging(int pageStart, int pageSize, Ordering[] orderings) {
        return new Paging(pageStart, pageSize, orderings);
    }
    
    public static Paging paging(int pageStart, int pageSize, Ordering ordering) {
        return new Paging(pageStart, pageSize, new Ordering[] { ordering });
    }
}
