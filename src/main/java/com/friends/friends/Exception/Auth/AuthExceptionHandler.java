package com.friends.friends.Exception.Auth;

import com.friends.friends.Exception.Account.AccountAlreadyExistsException;
import com.friends.friends.Exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(AccountAlreadyExistsException ex) {
        return new ResponseEntity<>(
            new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT.value()),
            HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return new ResponseEntity<>(
            new ErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED.value()),
            HttpStatus.UNAUTHORIZED
        );
    }
}
