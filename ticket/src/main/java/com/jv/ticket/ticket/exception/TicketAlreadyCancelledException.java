package com.jv.ticket.ticket.exception;

public class TicketAlreadyCancelledException extends RuntimeException {
    public TicketAlreadyCancelledException(String message) {
        super(message);
    }
}
