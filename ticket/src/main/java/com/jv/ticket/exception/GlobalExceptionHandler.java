package com.jv.ticket.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jv.ticket.ticket.exception.EventNotFoundException;
import com.jv.ticket.ticket.exception.EventServiceUnavailableException;
import com.jv.ticket.ticket.exception.InvalidCpfException;
import com.jv.ticket.ticket.exception.CpfMismatchException;
import com.jv.ticket.ticket.exception.TicketAlreadyCancelledException;
import com.jv.ticket.ticket.exception.TicketNotFoundException;
import com.jv.ticket.ticket.exception.UnauthorizedTicketAccessException;
import com.jv.ticket.user.exception.CpfViolationException;
import com.jv.ticket.user.exception.EmailUniqueViolationException;
import com.jv.ticket.user.exception.EmptyDataException;
import com.jv.ticket.user.exception.UserNotFoundException;
import com.jv.ticket.user.exception.UsernameUniqueViolationException;
import com.jv.ticket.user.exception.WrongPasswordException;

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

    // ========== USER EXCEPTIONS ==========

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

    @ExceptionHandler(CpfViolationException.class)
    public ResponseEntity<Map<String, Object>> handleCpfViolation(CpfViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());

        log.error("CPF violation: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ========== TICKET EXCEPTIONS ==========

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTicketNotFound(TicketNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());

        log.error("Ticket not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEventNotFound(EventNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());

        log.error("Event not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(TicketAlreadyCancelledException.class)
    public ResponseEntity<Map<String, Object>> handleTicketAlreadyCancelled(TicketAlreadyCancelledException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());

        log.error("Ticket already cancelled: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidCpfException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCpf(InvalidCpfException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());

        log.error("Invalid CPF: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(CpfMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleCpfMismatch(CpfMismatchException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());

        log.error("CPF mismatch: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(EventServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleEventServiceUnavailable(EventServiceUnavailableException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());

        log.error("Event service unavailable: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(UnauthorizedTicketAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedTicketAccess(UnauthorizedTicketAccessException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());

        log.error("Unauthorized ticket access: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ========== GENERIC EXCEPTION ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "An unexpected error occurred");

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
