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

package org.gogoup.dddutils.pool.resolver;

import org.gogoup.dddutils.pool.ParameterPool;

public class SimpleParameterResolver extends TransientParameterResolver {
    
    private Object parameter;
    
    public SimpleParameterResolver(String name) {
        super(new String[] { name });
        parameter = null;
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
        parameter = value;
        return true;
    }
    
    @Override
    public boolean isReadOnly(ParameterPool pool, String name) {
        return false;
    }
    
    @Override
    public void reset(ParameterPool pool) {
        parameter = null;
    }
    
    @Override
    public void clear(ParameterPool pool) {
        parameter = null;
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