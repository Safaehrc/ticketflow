package com.ticketflow.bookingservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(
        @NotNull Long eventId,
        @Min(1) @Max(10) int quantity,
        Long customerId,
        Long ticketTierId,
        String idempotencyKey) {

    public BookingRequest(Long eventId, int quantity) {
        this(eventId, quantity, null, null, null);
    }
}
