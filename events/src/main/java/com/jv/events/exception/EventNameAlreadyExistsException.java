package com.jv.events.exception;

public class EventNameAlreadyExistsException extends RuntimeException {
    public EventNameAlreadyExistsException(String eventName) {
        super("Já existe um evento com o nome: " + eventName);
    }
}
