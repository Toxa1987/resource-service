package com.epam.esm.resourceservice.exception;

public class S3ObjectNotFoundException extends RuntimeException {
    public S3ObjectNotFoundException(String message) {
        super(message);
    }
}
