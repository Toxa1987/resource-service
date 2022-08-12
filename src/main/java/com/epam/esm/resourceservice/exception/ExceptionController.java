package com.epam.esm.resourceservice.exception;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.ConnectException;


@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(value = {FileServiceException.class})
    public ResponseEntity<ApiExceptionResponse> getFileServiceExceptionData(
            FileServiceException exception) {
        return new ResponseEntity<>(getResponseEntity(exception), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ResourceValidationException.class})
    public ResponseEntity<ApiExceptionResponse> getResourceValidationException(
            ResourceValidationException exception) {
        return new ResponseEntity<>(getResponseEntity(exception), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {S3BucketNotExist.class})
    public ResponseEntity<ApiExceptionResponse> getS3BucketNotExistData(S3BucketNotExist exception) {
        return new ResponseEntity<>(getResponseEntity(exception), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {S3ObjectNotFoundException.class})
    public ResponseEntity<ApiExceptionResponse> getSS3ObjectNotFoundException(
            S3ObjectNotFoundException exception) {
        return new ResponseEntity<>(getResponseEntity(exception), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<ApiExceptionResponse> getConstraintViolationException(ConstraintViolationException exception) {
        return new ResponseEntity<>(getResponseEntity(exception), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ResourceNotFoundException.class})
    public ResponseEntity<ApiExceptionResponse> getResourceNotFoundException(ResourceNotFoundException exception) {
        return new ResponseEntity<>(getResponseEntity(exception), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {ConnectException.class})
    public ResponseEntity<ApiExceptionResponse> getConnectException(ConnectException exception) {
        return new ResponseEntity<>(getResponseEntity(exception), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(value = {BrokerUnavailableException.class})
    public ResponseEntity<ApiExceptionResponse> getBrokerUnavailableException(BrokerUnavailableException exception) {
        return new ResponseEntity<>(getResponseEntity(exception), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(value = {RuntimeException.class})
    public ResponseEntity<ApiExceptionResponse> getExceptionData(RuntimeException exception) {
        return new ResponseEntity<>(getResponseEntity(exception), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private ApiExceptionResponse getResponseEntity(Exception exception) {
        return ApiExceptionResponse.builder()
                .exception(exception.getClass().getName())
                .message(exception.getMessage())
                .build();
    }
}
