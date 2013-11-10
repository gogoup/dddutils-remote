package org.gogoup.dddutils.misc;

public class BusinessLogicException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = -4732388322445473267L;

	public BusinessLogicException(String message){
		super(message, null);
	}
	
	public BusinessLogicException(BusinessLogicException cause){
		super(null, cause);
	}
	
	public BusinessLogicException(String message, BusinessLogicException cause){
		super(message, cause); 
	}	
	
	public BusinessLogicException(Exception cause){
		super(null, cause);
	}
	/*
	public Throwable fillInStackTrace() {
		return this;		
	}*/
}
