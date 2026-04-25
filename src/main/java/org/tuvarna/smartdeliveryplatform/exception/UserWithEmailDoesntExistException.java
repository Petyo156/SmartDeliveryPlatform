package org.tuvarna.smartdeliveryplatform.exception;

public class UserWithEmailDoesntExistException extends RuntimeException {
    public UserWithEmailDoesntExistException(String message) {
        super(message);
    }
}
