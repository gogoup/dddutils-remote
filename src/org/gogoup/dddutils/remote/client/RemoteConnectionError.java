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
