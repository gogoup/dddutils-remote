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
	
		if(pageStart<1)
			throw new ProgrammingLogicException("Invalid page start value, >=1", null);
		if(pageSize<1 && pageSize!=-1)
			throw new ProgrammingLogicException("Invalid page size value, >=1 or -1", null);
		
		this.pageStart=pageStart;
		this.pageSize=pageSize;
		this.orderings=orderings;		
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
}
