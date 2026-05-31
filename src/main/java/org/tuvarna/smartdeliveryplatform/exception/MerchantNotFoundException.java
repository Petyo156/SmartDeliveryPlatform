package org.tuvarna.smartdeliveryplatform.exception;

public class MerchantNotFoundException extends RuntimeException {
    public MerchantNotFoundException(String message) {
        super(message);
    }
}
