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

package org.gogoup.dddutils.appsession;

import java.util.Date;

/**
 * This class represents a client access session for an application.
 * 
 * Each session has a unique key.
 * 
 * All persistentable parameters
 * {@link ParameterResolver#isTransient(ParameterPool, String)} in a session
 * should be able to replicated at other threads or JVM with the same session
 * key.
 * 
 * 
 */
public interface AppSession {
    
    public Object getParameter(String name);
    
    public boolean setParameter(String name, Object value);
    
    public Object[] findParameters(String query);    
    
    public String getApplicationId();
    
    public String getSessionKey();
    
    public Date getUpdateTime();
    
    /**
     * Updates the state of this session to what persisted with
     * {@link AppSession#sync()} before. If there is no session exist, then a
     * new session created.
     * 
     * @throws NoSuchAppSessionException
     *             When there is no persisted states can be retrieved
     */
    public void update() throws NoSuchAppSessionException;
    
    /**
     * Persist state of session.
     * 
     * @throws InconsistentAppSessionException
     *             TODO
     * @throws NoSuchAppSessionException
     *             TODO
     */
    public void sync() throws InconsistentAppSessionException, NoSuchAppSessionException;
    
    /**
     * delete this session.
     * 
     * @throws NoSuchAppSessionException
     * @throws InconsistentAppSessionException
     */
    public void destory() throws InconsistentAppSessionException, NoSuchAppSessionException;
    
    /**
     * return this session back to session pool.
     */
    public void close();
    
    public AppSessionContext getAppSessionContext();
    
}
