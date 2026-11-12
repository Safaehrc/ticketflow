package com.ticketflow.notificationservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Données métier d'un événement de réservation reçu depuis Kafka.
 * <p>
 * Copie du record {@code BookingEventPayload} du booking-service.
 * Dans un projet réel, ce record serait dans un module partagé.
 * </p>
 */
public record BookingEventPayload(
        String bookingReference,
        Long customerId,
        Long eventId,
        String eventName,
        Long tierId,
        String tierName,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        int quantity,
        String status,
        String idempotencyKey,
        Instant bookedAt,
        Instant expiresAt
) {}
