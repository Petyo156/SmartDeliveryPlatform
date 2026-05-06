package org.tuvarna.smartdeliveryplatform.exception;

public class PasswordsDoNotMatchException extends RuntimeException {
    public PasswordsDoNotMatchException(String message) {
        super(message);
    }

    public PasswordsDoNotMatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
