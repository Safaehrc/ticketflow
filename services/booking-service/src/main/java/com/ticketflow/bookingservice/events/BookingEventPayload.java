package com.ticketflow.bookingservice.events;

import com.ticketflow.bookingservice.entities.Booking;
import java.math.BigDecimal;
import java.time.Instant;

public record BookingEventPayload(
        Long bookingId,
        String bookingReference,
        Long customerId,
        Long eventId,
        String eventTitle,
        Long ticketTierId,
        String tierName,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        int quantity,
        String status,
        Instant createdAt,
        Instant expiresAt) {

    public static BookingEventPayload from(Booking b) {
        return new BookingEventPayload(
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
                b.getStatus().name(),
                b.getCreatedAt(),
                b.getExpiresAt()
        );
    }
}
