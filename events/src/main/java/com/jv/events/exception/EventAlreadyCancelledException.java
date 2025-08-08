package com.jv.events.exception;

public class EventAlreadyCancelledException extends RuntimeException {
    public EventAlreadyCancelledException(String eventId) {
        super(String.format("Event with ID '%s' is already cancelled", eventId));
    }
}
