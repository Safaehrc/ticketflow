package com.ticketflow.bookingservice.exceptions;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(Long eventId) {
        super("Event " + eventId + " not found or not bookable");
    }
}