package com.jv.events.exception;

public class EventCancellationNotAllowedException extends RuntimeException {
    
    public EventCancellationNotAllowedException(String eventId, long ticketCount) {
        super(String.format("Cannot cancel event '%s' because it has %d ticket(s) already purchased", eventId, ticketCount));
    }
}
