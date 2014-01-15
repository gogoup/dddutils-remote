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

package org.gogoup.dddutils.appsession.impl;

import org.gogoup.dddutils.appsession.AppService;
import org.gogoup.dddutils.appsession.AppServiceResolver;
import org.gogoup.dddutils.appsession.spi.AppSessionSPI;

public class MySQLAppSessionSPResolver implements AppServiceResolver {
    
    private static final String[] names = { AppSessionSPI.class.getCanonicalName() };
    
    public MySQLAppSessionSPResolver() {
    }
    
    @Override
    public String[] getServiceNames() {
        return names;
    }

    @Override
    public String[] findAppServiceNames(String query) {
        
        return null;
    }
    
    @Override
    public String[] getDependences(String name) {
        
        return null;
    }
    
    @Override
    public boolean verifyDependence(String name, AppService service) {
        
        return false;
    }
    
    @Override
    public AppService getAppService(String name) {
        
        if (AppSessionSPI.class.getCanonicalName().equals(name)) {
            return new MySQLAppSessionSP();
        }
        return null;
    }
    
}
