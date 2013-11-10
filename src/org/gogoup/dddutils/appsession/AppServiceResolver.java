package org.gogoup.dddutils.appsession;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gogoup.dddutils.pool.ParameterPool;
import org.gogoup.dddutils.pool.resolver.TransientParameterResolver;

public abstract class AppServiceResolver extends TransientParameterResolver {

	private AppSessionContext sessionContext;
	private Map<String, AppService> services;
	
	public AppServiceResolver(AppSessionContext sessionContext, String[] names) {
		super(names);
		this.sessionContext=sessionContext;
		
		this.services = new LinkedHashMap<String, AppService>(names.length);
		for(String name:names)
			this.services.put(name, null);
	}
	
	private AppSessionContext getSessionContext() {
		return sessionContext;
	}

	@Override
	public Object getParameter(ParameterPool pool, String name) {
		//System.out.println("HERE======>AppServiceResolver.getParameter() NAME: "+name);
		if(!this.services.containsKey(name)) return null;
		AppService service = this.services.get(name);
		if(null==service)
		{
			service = this.getAppService(this.getSessionContext(), name);
			if(null == service) return null;			
			this.services.put(name, service);
		}
		service.init(this.getSessionContext());
		return service;
	}
	
	public String[] findParameterNames(ParameterPool pool, String query) {
		return this.findAppServiceNames(this.getSessionContext(), query);
	}
	

	@Override
	public boolean setParameter(ParameterPool pool, String name, Object value) {
		
		return false;
	}

	@Override
	public boolean isReadOnly(ParameterPool pool, String name) {
		return true;
	}

	@Override
	public void reset(ParameterPool pool) {		
	}

	@Override
	public void clear(ParameterPool pool) {
		services.clear();
	}
	
	@Override
	public String[] getDependences(ParameterPool pool, String name) {
		return this.getDependences(name);
	}
	
	@Override
	public boolean verifyDependence(ParameterPool pool, String name, Object parameter) {
		return this.verifyDependence(name, parameter);
	}

	abstract protected AppService getAppService(AppSessionContext context, String name);
	
	abstract protected String[] findAppServiceNames(AppSessionContext context, String query);

	abstract protected String[] getDependences(String name);
	
	abstract protected boolean verifyDependence(String name, Object value);
}
