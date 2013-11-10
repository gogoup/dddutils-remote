package org.gogoup.dddutils.misc;

/**
 * This exception is used to represent some programmatically issue happened.
 * 
 * 
 *
 */
public class ProgrammingLogicException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6610588898203155711L;

	public ProgrammingLogicException(String message){
		super(message, null);		
	}
	
	public ProgrammingLogicException(ProgrammingLogicException cause){
		super(null, cause);
	}
	
	public ProgrammingLogicException(String message, ProgrammingLogicException cause){
		super(message, cause);
	}
		
	/*
	public Throwable fillInStackTrace() {
		return this;		
	}*/	
	
	
}
