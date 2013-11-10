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
