package com.epam.esm.resourceservice.exception;

public class S3BucketNotExist extends RuntimeException {
    public S3BucketNotExist(String message) {
        super(message);
    }
}
