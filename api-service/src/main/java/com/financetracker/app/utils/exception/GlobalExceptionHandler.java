package com.financetracker.app.utils.exception;

import com.financetracker.app.utils.exception.custom.DocumentNotFoundException;
import com.financetracker.app.utils.exception.custom.IdNotMatchException;
import com.financetracker.app.utils.exception.custom.UserAlreadyExistException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<Object> handleDocumentNotFoundException(Throwable exception) {
        return buildResponseEntity(new ErrorResponse(NOT_FOUND, "Document not found", exception));
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<Object> handleUserAlreadyExistException(Throwable exception) {
        return buildResponseEntity(new ErrorResponse(CONFLICT, "Failed registration - user already exist", exception));
    }

        @ExceptionHandler(IdNotMatchException.class)
    public ResponseEntity<Object> handleIdNotMatchException(Throwable exception) {
        return buildResponseEntity(new ErrorResponse(CONFLICT, "Response body id and URL id doesn't match", exception));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<Object> handleFailedDTOValidationExceptions(Throwable exception) {
        return buildResponseEntity(new ErrorResponse(UNPROCESSABLE_ENTITY, "DTO failed validation", exception));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(Throwable exception) {
        return buildResponseEntity(new ErrorResponse(UNAUTHORIZED, "Wrong login or password", exception));
    }

    private ResponseEntity<Object> buildResponseEntity(ErrorResponse errorResponse) {
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }
}