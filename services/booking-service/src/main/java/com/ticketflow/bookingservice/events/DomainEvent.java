package com.ticketflow.bookingservice.events;

import java.time.Instant;
import java.util.UUID;

public record DomainEvent<T>(
        String eventId,
        String eventType,
        String schemaVersion,
        Instant timestamp,
        String aggregateId,
        String correlationId,
        T payload) {

    public static <T> DomainEvent<T> of(String eventType, String aggregateId, T payload) {
        return new DomainEvent<>(
                UUID.randomUUID().toString(),
                eventType,
                "1.0",
                Instant.now(),
                aggregateId,
                UUID.randomUUID().toString(),
                payload
        );
    }
}
