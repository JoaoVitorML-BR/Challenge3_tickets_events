package com.jv.ticket.ticket.exception;

public class EventServiceUnavailableException extends RuntimeException {
    public EventServiceUnavailableException(String message) {
        super(message);
    }
    
    public EventServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
