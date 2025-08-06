package com.jv.ticket.user.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        
        response.put("status", "error");
        response.put("message", "Validation failed");
        response.put("errors", errors);
        
        log.error("Validation error: {}", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Access denied. You can only access your own user information.");
        
        log.error("Authorization denied: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(UsernameUniqueViolationException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameUniqueViolation(UsernameUniqueViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        
        log.error("Username unique violation: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(EmailUniqueViolationException.class)
    public ResponseEntity<Map<String, Object>> handleEmailUniqueViolation(EmailUniqueViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        
        log.error("Email unique violation: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        
        log.error("User not found: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<Map<String, Object>> handleWrongPassword(WrongPasswordException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        
        log.error("Wrong password: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(EmptyDataException.class)
    public ResponseEntity<Map<String, Object>> handleEmptyData(EmptyDataException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        
        log.error("Empty data: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "An unexpected error occurred");
        
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
