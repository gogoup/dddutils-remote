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

import java.util.Date;
import java.util.Map;

import org.gogoup.dddutils.objectsegment.OSDirtyMarker;
import org.gogoup.dddutils.objectsegment.ObjectSegment;

/**
 * This class defines the data structure of a session.
 * 
 * @author ruisun
 * 
 */
public class AppSessionOS extends OSDirtyMarker {
    
    private String applicationId;    
    private String sessionKey;
    private Map<String, Object> states;
    private Date lastUpdateTime;
    
    public AppSessionOS(String applicationId, String sessionKey, 
            Map<String, Object> states, Date lastUpdateTime) {
        super(null);
        this.applicationId = applicationId;
        this.sessionKey = sessionKey;
        this.states = states;
        this.lastUpdateTime = lastUpdateTime;
        this.cleanMarks();
    }
    
    public String getApplicationId() {
        return applicationId;
    }
    
    public String getSessionKey() {
        return sessionKey;
    }
    
    public Map<String, Object> getStates() {
        return states;
    }
    
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public void setStates(Map<String, Object> states) {
        this.states = states;
        this.markDirty();
    }
    
    @Override
    public ObjectSegment copy() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public ObjectSegment copy(ObjectSegment parent) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
