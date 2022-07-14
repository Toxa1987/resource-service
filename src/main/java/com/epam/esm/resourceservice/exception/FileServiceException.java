package com.epam.esm.resourceservice.exception;

public class FileServiceException extends RuntimeException {
    public FileServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileServiceException(String message) {
        super(message);
    }
}
