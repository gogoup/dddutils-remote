package org.gogoup.dddutils.objectsegment;

public enum OSTransactionStatus {
    START("OS_TA_START"), 
    COMMIT("OS_TA_COMMIT"), 
    ROLLBACK("OS_TA_ROLLBACK");
    
    private String value;
    
    private OSTransactionStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static OSTransactionStatus toStatus(String value) {
        
        if (value.equals("OS_TA_START"))
            return START;
        else if (value.equals("OS_TA_COMMIT"))
            return COMMIT;
        else if (value.equals("OS_TA_ROLLBACK"))
            return ROLLBACK;
        
        return null;
    }
}
