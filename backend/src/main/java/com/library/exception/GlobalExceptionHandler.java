package com.library.exception;

import com.library.dto.Dtos.MessageResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<MessageResponse> api(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(new MessageResponse(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse> validation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new MessageResponse(msg));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<MessageResponse> badCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Invalid username or password"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MessageResponse> integrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse("Data conflict, please retry"));
    }
}
