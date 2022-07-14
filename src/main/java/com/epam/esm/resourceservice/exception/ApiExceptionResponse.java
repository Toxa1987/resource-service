package com.epam.esm.resourceservice.exception;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApiExceptionResponse {
    String message;
    String exception;
}
