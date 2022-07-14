package com.epam.esm.resourceservice.exception;

public class BrokerUnavailableException extends RuntimeException {
    public BrokerUnavailableException(String message) {
        super(message);
    }

    public BrokerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
