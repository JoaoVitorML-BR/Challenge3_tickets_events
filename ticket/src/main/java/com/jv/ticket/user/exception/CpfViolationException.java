package com.jv.ticket.user.exception;

public class CpfViolationException extends RuntimeException {
    public CpfViolationException(String message) {
        super(message);
    }
}
