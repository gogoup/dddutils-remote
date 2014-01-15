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

import java.util.Date;

import org.gogoup.dddutils.pool.ParameterPool;
import org.gogoup.dddutils.pool.ParameterResolver;
import org.gogoup.dddutils.pool.impl.DefaultParameterPool;

public abstract class OSDirtyMarker implements ObjectSegment {
    
    private boolean isDirty;
    private boolean isNew;
    private boolean isDeleted;
    protected ObjectSegment parent;
    protected ParameterPool caches;
    private Date creationTime;
    private Date modificationTime;
    
    public OSDirtyMarker(ObjectSegment parent, Date creationTime, Date modificationTime) {
        caches = new DefaultParameterPool(new ParameterResolver[0]);
        this.setParentDS(parent);
        this.creationTime = creationTime;
        this.modificationTime = modificationTime;
    }
    
    public OSDirtyMarker(ObjectSegment parent) {
        this(parent, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));
    }
    
    @Override
    public void markDirty() {
        if (isDeleted())
            throw new IllegalStateException("Cannot mark void data segment.");
        if (isNew())
            return;
        cleanMarks();
        isDirty = true;
        this.modificationTime = new Date(System.currentTimeMillis());
    }
    
    @Override
    public boolean isDirty() {
        return isDirty;
    }
    
    @Override
    public void markNew() {
        if (isDeleted())
            throw new IllegalStateException("Cannot mark void data segment.");
        cleanMarks();
        isNew = true;
    }
    
    @Override
    public boolean isNew() {
        return isNew;
    }
    
    @Override
    public void markDeleted() {
        cleanMarks();
        if (isNew())
            return;
        isDeleted = true;
    }
    
    @Override
    public boolean isDeleted() {
        return isDeleted;
    }
    
    @Override
    public boolean isCleanMark() {
        return (!this.isNew() && !this.isDirty() && !this.isDeleted());
    }
    
    @Override
    public Date getCreationTime() {
        return this.creationTime;
    }
    
    @Override
    public Date getModificationTime() {
        return this.modificationTime;
    }
    
    @Override
    public void cleanMarks() {
        isDirty = false;
        isNew = false;
        isDeleted = false;
    }
    
    @Override
    public void setCache(String name, Object value) {
        caches.setParameter(name, value);
    }
    
    @Override
    public Object getCache(String name) {
        return this.caches.getParameter(name);
    }
    
    @Override
    public void clean() {
        
        this.cleanMarks();
        this.caches.clear();
    }
    
    // @Override
    public void setParentDS(ObjectSegment parent) {
        if (null != this.parent)
            throw new IllegalStateException(
                    "Change existing parent may cause inconsistency issue, try \"copy(parent)\".");
        this.parent = parent;
    }
    
    @Override
    public ObjectSegment getParentOS() {
        return this.parent;
    }
    
    @Override
    public String[] getNames() {
        return this.caches.getParameterNames();
    }
    
    protected void copyTo(OSDirtyMarker ds, ObjectSegment parent) {
        ds.isDirty = this.isDirty;
        ds.isNew = this.isNew;
        ds.isDeleted = this.isDeleted;
        ds.parent = this.parent;
        ds.creationTime = this.creationTime;
        ds.modificationTime = this.modificationTime;
    }
    
    @Override
    public Object removeCache(String name) {
        Object obj = this.caches.getParameter(name);
        this.caches.setParameter(name, null);
        return obj;
    }
    
    @Override
    public ObjectSegment copy() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public ObjectSegment copy(ObjectSegment parent) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
