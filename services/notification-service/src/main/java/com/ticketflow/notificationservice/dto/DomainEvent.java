package com.ticketflow.notificationservice.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Enveloppe générique d'un événement de domaine publié sur le bus Kafka.
 * <p>
 * Cette classe est une copie de {@code com.ticketflow.bookingservice.events.DomainEvent}.
 * Dans un projet réel, elle serait dans un module partagé (ex : {@code ticketflow-events-api}).
 * </p>
 *
 * @param <T> type du payload métier
 */
public record DomainEvent<T>(
        /** Identifiant unique de cet événement (UUID v4). */
        String eventId,
        /** Type sémantique de l'événement (ex : "BookingCreated"). */
        String eventType,
        /** Version du schéma pour la compatibilité ascendante. */
        int schemaVersion,
        /** Horodatage UTC de la production de l'événement. */
        Instant timestamp,
        /** Référence de l'agrégat source (ex : "BK-XXXXXXXX"). */
        String aggregateId,
        /** Identifiant de corrélation pour le tracing distribué. */
        String correlationId,
        /** Données métier de l'événement. */
        T payload
) {
    /** Constructeur de confort avec valeurs par défaut. */
    public static <P> DomainEvent<P> of(String eventType, String aggregateId, P payload) {
        return new DomainEvent<>(
                UUID.randomUUID().toString(),
                eventType,
                1,
                Instant.now(),
                aggregateId,
                UUID.randomUUID().toString(),
                payload
        );
    }
}
