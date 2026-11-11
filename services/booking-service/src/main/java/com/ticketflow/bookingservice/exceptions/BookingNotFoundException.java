package com.ticketflow.bookingservice.exceptions;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(Long id) {
        super("Booking not found: " + id);
    }

    public BookingNotFoundException(String reference) {
        super("Booking not found: " + reference);
    }
}
