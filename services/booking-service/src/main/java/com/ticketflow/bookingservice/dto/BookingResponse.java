package com.ticketflow.bookingservice.dto;

import com.ticketflow.bookingservice.entities.Booking;
import com.ticketflow.bookingservice.entities.BookingStatus;
import java.time.Instant;

public record BookingResponse(Long id, Long eventId, String eventTitle, int quantity,
                              BookingStatus status, Instant expiresAt) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getId(), b.getEventId(), b.getEventTitle(),
                b.getQuantity(), b.getStatus(), b.getExpiresAt());
    }
}