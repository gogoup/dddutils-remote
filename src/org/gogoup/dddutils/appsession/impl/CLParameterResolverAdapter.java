package org.gogoup.dddutils.appsession.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.gogoup.dddutils.pool.ParameterPool;
import org.gogoup.dddutils.pool.ParameterResolver;


public class CLParameterResolverAdapter implements ParameterResolver {
	
	private String[] names;
	private Map<String, String> clzNameDictionary;
	private Map<String, ParameterResolver> resolverDictionary;
	private CLAppSession session;
	private String rootPath;
	
	public CLParameterResolverAdapter(CLAppSession session, String[] names, String rootPath, String[] clzNames) {
		
		this.names = names;
		this.session = session;
		this.clzNameDictionary = new HashMap<String, String>();
		this.resolverDictionary = new HashMap<String, ParameterResolver>();
		this.rootPath=rootPath;
		for(int i=0; i<clzNames.length; i++)
		{
			this.clzNameDictionary.put(names[i], clzNames[i]);	
		}
	}
	
	private ParameterResolver getParameterResolver(String name) {
		//System.out.println("HERE======>CLParameterResolverAdapter.getAppService() #1 context: "+context);
		//System.out.println("HERE======>CLParameterResolverAdapter.getAppService() #1 name: "+name);
		String clzName = this.clzNameDictionary.get(name);
		if(null == clzName) return null;
		
		ParameterResolver resolver = null;
		try {
			Class<?> clz = this.session.loadClassByName(this.rootPath, clzName);
			resolver = (ParameterResolver) clz.newInstance();
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return resolver;
	}
	
	private ParameterResolver fetchNotNullResolver(String name) {
		
		ParameterResolver resolver = this.resolverDictionary.get(name);
		if(null == resolver)
		{
			resolver = this.getParameterResolver(name);
			this.resolverDictionary.put(name, resolver);
		}
		
		return resolver;
	}

	
	@Override
	public String[] getParameterNames(ParameterPool pool) {return names;}
	
	@Override
	public Object getParameter(ParameterPool pool, String name) {
		
		return this.fetchNotNullResolver(name).getParameter(pool, name);
	}
	
	public boolean setParameter(ParameterPool pool, String name, Object value) {
		return this.fetchNotNullResolver(name).setParameter(pool, name, value);
	}

	
	public boolean isReadOnly(ParameterPool pool, String name) {
		return this.fetchNotNullResolver(name).isReadOnly(pool, name);
	}
	
	public boolean isTransient(ParameterPool pool, String name) {
		return this.fetchNotNullResolver(name).isTransient(pool, name);
	}
		
	public void reset(ParameterPool pool) {
		
		for(Iterator<ParameterResolver> iter = this.resolverDictionary.values().iterator(); iter.hasNext();)
		{
			ParameterResolver resolver = iter.next();
			resolver.reset(pool);
		}
	}
	
	/**
	 * Clear values
	 */
	public void clear(ParameterPool pool) {
		
		for(Iterator<ParameterResolver> iter = this.resolverDictionary.values().iterator(); iter.hasNext();)
		{
			ParameterResolver resolver = iter.next();
			resolver.clear(pool);
		}
		
		this.clzNameDictionary.clear();
		this.clzNameDictionary = null;
		this.resolverDictionary.clear();
		this.resolverDictionary = null;
		this.session = null;
		this.rootPath = null;
	}
	
	public Object getStates(ParameterPool pool, String name) {
		return this.fetchNotNullResolver(name).getStates(pool, name);
	}
	
	public void restoreStates(ParameterPool pool, String name, Object state) {
		this.fetchNotNullResolver(name).restoreStates(pool, name, state);
	}
		
	public String[] getDependences(ParameterPool pool, String name) {
		return this.fetchNotNullResolver(name).getDependences(pool, name);
	}
	
	public boolean verifyDependence(ParameterPool pool, String name, Object parameter) {
		return this.fetchNotNullResolver(name).verifyDependence(pool, name, parameter);
	}

	@Override
	public String[] findParameterNames(ParameterPool pool, String query) {
		
		List<String> nameList = new LinkedList<String>();
		
		for(Iterator<ParameterResolver> iter = this.resolverDictionary.values().iterator(); iter.hasNext();)
		{
			ParameterResolver resolver = iter.next();
			String[] names = resolver.findParameterNames(pool, query);
			if(null == names) continue;
			for(String name: names)
			{
				nameList.add(name);
			}
		}
		
		return nameList.toArray(new String[nameList.size()]);
	}
	
}
