package org.gogoup.dddutils.pool.resolver;

import org.gogoup.dddutils.pool.ParameterPool;
import org.gogoup.dddutils.pool.ParameterResolver;


public abstract class TransientParameterResolver implements ParameterResolver {

	private String[] names;
	
	public TransientParameterResolver(String[] names) {
		this.names = names;
	}
	
	@Override
	public String[] getParameterNames(ParameterPool pool) {return names;}
	
	@Override
	public boolean isTransient(ParameterPool pool, String name) {
		return true;
	}

	@Override
	public Object getStates(ParameterPool pool, String name) {
		throw new UnsupportedOperationException("due to parameter, "+name+", is transient.");
	}

	@Override
	public void restoreStates(ParameterPool pool, String name, Object state) {
		throw new UnsupportedOperationException("due to parameter, "+name+", is transient.");
	}

}
