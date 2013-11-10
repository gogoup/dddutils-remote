package org.gogoup.dddutils.appsession.impl;

import org.gogoup.dddutils.appsession.AppService;
import org.gogoup.dddutils.appsession.AppServiceResolver;
import org.gogoup.dddutils.appsession.AppSessionContext;
import org.gogoup.dddutils.appsession.spi.AppSessionSPI;

public class MySQLAppSessionSPResolver extends AppServiceResolver {
	
	private static final String[] names={
		AppSessionSPI.class.getCanonicalName()
	};
	

	public MySQLAppSessionSPResolver(AppSessionContext sessionContext) {
		super(sessionContext, names);
		
	}

	@Override
	protected String[] findAppServiceNames(AppSessionContext context,
			String query) {

		return null;
	}

	@Override
	protected String[] getDependences(String name) {
		
		return null;
	}

	@Override
	protected boolean verifyDependence(String name, Object value) {
		
		
		return false;
	}

	@Override
	protected AppService getAppService(AppSessionContext context,
			String name) {
		
		if(AppSessionSPI.class.getCanonicalName().equals(name))
		{				
			return new MySQLAppSessionSP();
		}
		return null;
	}

}
