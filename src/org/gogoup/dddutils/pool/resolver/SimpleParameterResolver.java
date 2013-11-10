package org.gogoup.dddutils.pool.resolver;

import org.gogoup.dddutils.pool.ParameterPool;



public class SimpleParameterResolver extends TransientParameterResolver{
	
	private Object parameter;		
		
	public SimpleParameterResolver(String name) {
		super(new String[]{name});	
		parameter=null;			
	}	
	
	@Override
	public Object getParameter(ParameterPool pool, String name) {
		return parameter;
	}

	@Override
	public String[] findParameterNames(ParameterPool pool, String query) {
		return null;
	}

	@Override
	public boolean setParameter(ParameterPool pool, String name, Object value) {		
		parameter=value;
		return true;
	}			

	@Override
	public boolean isReadOnly(ParameterPool pool, String name) {return false;}

	@Override
	public void reset(ParameterPool pool) {			
		parameter=null;
	}

	@Override
	public void clear(ParameterPool pool) {
		parameter=null;
	}

	@Override
	public String[] getDependences(ParameterPool pool, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verifyDependence(ParameterPool pool, String name, Object parameter) {
		return false;
	}

}