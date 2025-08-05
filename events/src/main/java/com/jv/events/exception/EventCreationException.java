package com.jv.events.exception;

public class EventCreationException extends RuntimeException {
    public EventCreationException(String message, Throwable cause) {
        super("Erro ao criar evento: " + message, cause);
    }
    
    public EventCreationException(String message) {
        super("Erro ao criar evento: " + message);
    }
}
