package com.ticketflow.bookingservice.exceptions;

public class EventServiceUnavailableException extends RuntimeException {

    public EventServiceUnavailableException(Throwable cause) {
        super("Event service is currently unavailable", cause);
    }
}