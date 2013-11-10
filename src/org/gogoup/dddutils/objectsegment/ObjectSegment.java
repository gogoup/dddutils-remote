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


public interface ObjectSegment {
	
	public void markDirty();
	public boolean isDirty();
	
	public void markNew();
	public boolean isNew();
	
	public void markDeleted();
	public boolean isDeleted();
	
	public boolean isCleanMark();
	
	public Date getCreationTime();	
	public Date getModificationTime();
	
	/**
	 * Clear the dirty marks for self only.
	 */
	public void cleanMarks();
	
	public void clean();
	
	public ObjectSegment copy();
	public ObjectSegment copy(ObjectSegment parent); //copy self to new parent. 	
	public ObjectSegment getParentOS();	
	
	public void setCache(String name, Object value);
	public Object getCache(String name);
	public Object removeCache(String name);
	public String[] getNames();
	
}
