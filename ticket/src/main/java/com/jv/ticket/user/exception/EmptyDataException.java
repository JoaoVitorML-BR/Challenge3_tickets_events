package com.jv.ticket.user.exception;

public class EmptyDataException extends RuntimeException {
    public EmptyDataException(String message) {
        super(message);
    }
}
