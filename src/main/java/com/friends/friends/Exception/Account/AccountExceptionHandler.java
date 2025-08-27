package com.friends.friends.Exception.Account;

import com.friends.friends.Exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AccountExceptionHandler {
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(AccountNotFoundException ex) {
        return new ResponseEntity<>(
            new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()),
            HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(InvalidAccountUpdateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUpdate(InvalidAccountUpdateException ex) {
        return new ResponseEntity<>(
            new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()),

            HttpStatus.BAD_REQUEST
        );
    }
}
