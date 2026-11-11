package com.ticketflow.bookingservice.dto;

import com.ticketflow.bookingservice.entities.Booking;
import com.ticketflow.bookingservice.entities.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record BookingResponse(
        Long id,
        String bookingReference,
        Long customerId,
        Long eventId,
        String eventTitle,
        Long ticketTierId,
        String tierName,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        int quantity,
        BookingStatus status,
        String idempotencyKey,
        Instant createdAt,
        Instant expiresAt) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getBookingReference(),
                b.getCustomerId(),
                b.getEventId(),
                b.getEventTitle(),
                b.getTicketTierId(),
                b.getTierName(),
                b.getUnitPrice(),
                b.getTotalPrice(),
                b.getQuantity(),
                b.getStatus(),
                b.getIdempotencyKey(),
                b.getCreatedAt(),
                b.getExpiresAt()
        );
    }
}
