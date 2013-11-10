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

package org.gogoup.dddutils.remote.client;

public enum RemoteConnectionError {
	
	NONE(0), CONNECTION_FAILED(1), TIMEOUT(2), HANDSHAKE_FAILED(3);
	
    private int value;

    private RemoteConnectionError(int value) {
            this.value = value;
    }
	
    public int getValue() {return this.value;}
    
    public static RemoteConnectionError getError(int value) {
    	switch (value) {
    		case 0:
    			return NONE;
        	case 1:
                return CONNECTION_FAILED;
        	case 2:
                return TIMEOUT;
        	case 3:
                return HANDSHAKE_FAILED;
    	}
    	
    	throw new IllegalArgumentException("Invalid value (0 - 3), "+value);
    }
}
