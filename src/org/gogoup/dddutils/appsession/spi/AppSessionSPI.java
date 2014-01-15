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

package org.gogoup.dddutils.appsession.spi;

/**
 * This interface class specifies the behaviors of how upper layers access the
 * persistent data.
 * 
 * Generalization class needs to provide persistent functions for session
 * states.
 * 
 * @author ruisun
 * 
 */

public interface AppSessionSPI {
    
    /**
     * Check if the giving session key has been taken under the same application
     * id.
     * 
     * @param applicationId
     *            String
     * @param sessionKey
     *            String
     * @return boolean Return true if the key has been taken, otherwise, return
     *         false.
     */
    public boolean hasSession(String applicationId, String sessionKey);
    
    /**
     * Save the giving session object segment to persistent media.
     * 
     * @param os
     *            AppSessionOS
     */
    public void insert(AppSessionOS os);
    
    /**
     * Update the exiting session object segment with the specified one.
     * 
     * @param os
     *            AppSessionOS
     */
    public void update(AppSessionOS os);
    
    /**
     * Delete the specified session object segment from persistent media
     * 
     * @param os
     *            AppSessionOS
     */
    public void delete(AppSessionOS os);
    
    /**
     * Return a session object segment with the giving session key under the
     * specified application id.
     * 
     * @param applicationId
     *            String
     * @param sessionKey
     *            String
     * @return AppSessionOS Return null if there is no such session exits.
     */
    public AppSessionOS selectByKey(String applicationId, String sessionKey);
    
}
