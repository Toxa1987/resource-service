package com.epam.esm.resourceservice.exception;

public class ResourceValidationException extends RuntimeException {
    public ResourceValidationException(String message) {
        super(message);
    }
}
