package com.friends.friends.Exception.Location;

import com.friends.friends.Exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class LocationExceptionHandler {
    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLocationNotFound(LocationNotFoundException ex) {
        return new ResponseEntity<>(
            new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()),
            HttpStatus.NOT_FOUND
        );
    }
}
