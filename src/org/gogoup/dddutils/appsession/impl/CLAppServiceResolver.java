package org.gogoup.dddutils.appsession.impl;

import java.util.HashMap;
import java.util.Map;

import org.gogoup.dddutils.appsession.AppService;
import org.gogoup.dddutils.appsession.AppServiceResolver;
import org.gogoup.dddutils.appsession.AppSessionContext;


public abstract class CLAppServiceResolver extends AppServiceResolver {
	
	private Map<String, String> clzNameDictionary;
	private CLAppSession session;
	private String rootPath;
	
	public CLAppServiceResolver(AppSessionContext sessionContext, String[] names, String rootPath, String[] clzNames) {
		super(sessionContext, names);
		this.session = (CLAppSession) sessionContext.getCurrentSession();
		this.clzNameDictionary = new HashMap<String, String>();
		this.rootPath=rootPath;
		for(int i=0; i<clzNames.length; i++)
		{
			this.clzNameDictionary.put(names[i], clzNames[i]);	
		}
			
	}
	
	@Override
	protected AppService getAppService(AppSessionContext context, String name) {
		//System.out.println("HERE======>CLAppServiceResolver.getAppService() #1 context: "+context);
		//System.out.println("HERE======>CLAppServiceResolver.getAppService() #1 name: "+name);
		String clzName = this.clzNameDictionary.get(name);
		if(null == clzName) return null;
		
		AppService service = null;
		try {
			Class<?> clz = this.session.loadClassByName(this.rootPath, clzName);
			service = (AppService) clz.newInstance();
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return service;
	}

}
