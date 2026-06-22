package org.tuvarna.smartdeliveryplatform.exception;

public class UserWithPhoneNumberAlreadyExistsException extends RuntimeException {
    public UserWithPhoneNumberAlreadyExistsException(String message) {
        super(message);
    }
}
