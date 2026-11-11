package com.ticketflow.bookingservice.exceptions;

public class BookingExpiredException extends RuntimeException {
    public BookingExpiredException(Long id) {
        super("Booking has expired: " + id);
    }
}
