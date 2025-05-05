package org.example.qaservice.config;

import org.example.qacommon.entity.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex,
            WebRequest request) {

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(new ErrorResponse(
                        (HttpStatus) ex.getStatusCode(),
                        ex.getReason(),
                        request.getDescription(false).replace("uri=", "")
                ));
    }
}