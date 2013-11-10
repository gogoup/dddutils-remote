/*
 * Copyright 2013 Rui Sun (SteveSunCanada@gmail.com)
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

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
