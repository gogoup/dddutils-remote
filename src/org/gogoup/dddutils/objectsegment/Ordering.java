package org.gogoup.dddutils.objectsegment;

public class Ordering {

	public final static int ORDER_ASC=0;
	public final static int ORDER_DESC=1;
	
	private int order;
	private String attribute;	
	
	public Ordering(String attribute, int order) {
		this.attribute=attribute;
		this.order=order;
	}

	public int getOrder() {
		return order;
	}

	public String getAttribute() {
		return attribute;
	}
}
