package org.tuvarna.smartdeliveryplatform.exception;

public class UserWithEmailAlreadyExistsException extends RuntimeException {
    public UserWithEmailAlreadyExistsException(String message) {
        super(message);
    }
}