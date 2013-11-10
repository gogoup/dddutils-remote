package example.org.gogoup.dddutils;

import org.gogoup.dddutils.appsession.AppService;
import org.gogoup.dddutils.appsession.AppServiceResolver;
import org.gogoup.dddutils.appsession.AppSessionContext;

public class SimplServiceResolver extends AppServiceResolver {

	private static final String[] names={		
		"Simple"
	};
	

	public SimplServiceResolver(AppSessionContext sessionContext) {
		super(sessionContext, names);
		
	}

	@Override
	protected String[] findAppServiceNames(AppSessionContext context,
			String query) {

		if(query.replace("/", "").equalsIgnoreCase("Simple".toLowerCase()))
			return new String[]{"Simple"};
		
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
		
		if("Simple".equals(name))
			return new SimpleService();

		return null;
	}

}
