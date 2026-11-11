package com.ticketflow.bookingservice.clients;

import java.math.BigDecimal;

public record CapacityReservationResponse(
        Long eventId,
        String eventTitle,
        Long ticketTierId,
        String tierName,
        BigDecimal unitPrice,
        int quantity,
        int remainingCapacity) { }
