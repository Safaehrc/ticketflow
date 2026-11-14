package com.ticketflow.notificationservice.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketflow.notificationservice.dto.BookingEventPayload;
import com.ticketflow.notificationservice.dto.DomainEvent;
import com.ticketflow.notificationservice.entities.NotificationChannel;
import com.ticketflow.notificationservice.entities.NotificationLog;
import com.ticketflow.notificationservice.entities.NotificationStatus;
import com.ticketflow.notificationservice.repos.NotificationLogRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Consommateur Kafka des événements de réservation.
 *
 * <p>
 * Chaque message reçu est traité de façon <strong>idempotente</strong> :
 * si l'eventId a déjà été traité (présent en base), le message est ignoré.
 * Cela protège contre la livraison en double (at-least-once delivery de Kafka).
 * </p>
 *
 * <p>
 * Pour l'instant, les notifications sont <em>simulées</em> (log uniquement).
 * Dans les étapes suivantes (step-10-mcp), on intégrera un vrai canal Telegram.
 * </p>
 */
@Component
public class BookingNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingNotificationConsumer.class);

    private final NotificationLogRepo repo;
    private final ObjectMapper objectMapper;

    public BookingNotificationConsumer(NotificationLogRepo repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    /**
     * Point d'entrée Kafka : un message de type {@code DomainEvent} est reçu depuis
     * le topic {@code ticketflow.bookings}.
     *
     * @param rawEvent map désérialisée depuis le JSON Kafka
     */
    @KafkaListener(
            topics = "${ticketflow.kafka.topics.bookings:ticketflow.bookings}",
            groupId = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onBookingEvent(Map<String, Object> rawEvent) {
        // ---- Extraction des champs de l'enveloppe ----
        String eventId   = extractString(rawEvent, "eventId");
        String eventType = extractString(rawEvent, "eventType");
        String aggregateId = extractString(rawEvent, "aggregateId");

        log.info("Kafka event received: type={}, eventId={}, aggregateId={}",
                eventType, eventId, aggregateId);

        if (eventId == null || eventType == null) {
            log.warn("Message Kafka invalide reçu : eventId ou eventType absent. Ignoré.");
            return;
        }

        // ---- Déduplication idempotente ----
        if (repo.findByEventId(eventId).isPresent()) {
            log.info("Événement {} déjà traité (idempotence). Ignoré.", eventId);
            return;
        }

        // ---- Extraction du payload métier ----
        BookingEventPayload payload = extractPayload(rawEvent);
        String recipient = payload != null
                ? String.valueOf(payload.customerId())
                : "unknown";

        // ---- Construction du message de notification ----
        String subject = buildSubject(eventType, aggregateId);
        String content = buildContent(eventType, aggregateId, payload);

        // ---- Simulation d'envoi (log) ----
        log.info("[NOTIFICATION SIMULÉE] Canal=EMAIL | Destinataire={} | Objet='{}' | Contenu={}",
                recipient, subject, content);

        // ---- Persistance de la trace ----
        NotificationLog notifLog = new NotificationLog(
                eventId,
                aggregateId != null ? aggregateId : "UNKNOWN",
                recipient,
                NotificationChannel.EMAIL,
                subject,
                content,
                NotificationStatus.SENT
        );
        repo.save(notifLog);

        log.info("Notification enregistrée en base pour l'événement {} (booking {})",
                eventType, aggregateId);
    }

    // ---- Méthodes utilitaires privées ----

    private String extractString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    @SuppressWarnings("unchecked")
    private BookingEventPayload extractPayload(Map<String, Object> rawEvent) {
        try {
            Object payloadObj = rawEvent.get("payload");
            if (payloadObj instanceof Map) {
                return objectMapper.convertValue(payloadObj, BookingEventPayload.class);
            }
        } catch (Exception ex) {
            log.warn("Impossible de désérialiser le payload : {}", ex.getMessage());
        }
        return null;
    }

    private String buildSubject(String eventType, String aggregateId) {
        return switch (eventType) {
            case "BookingCreated"   -> "Réservation créée — " + aggregateId;
            case "BookingConfirmed" -> "Réservation confirmée — " + aggregateId;
            case "BookingExpired"   -> "Réservation expirée — " + aggregateId;
            case "BookingCancelled" -> "Réservation annulée — " + aggregateId;
            default -> "Événement de réservation — " + aggregateId;
        };
    }

    private String buildContent(String eventType, String aggregateId, BookingEventPayload payload) {
        if (payload == null) {
            return "Événement " + eventType + " reçu pour la réservation " + aggregateId + ".";
        }
        return switch (eventType) {
            case "BookingCreated" -> String.format(
                    "Votre réservation %s pour '%s' (catégorie %s, %d place(s)) a été créée. " +
                    "Montant total : %.2f €. Elle expire le %s.",
                    aggregateId, payload.eventTitle(), payload.tierName(),
                    payload.quantity(), payload.totalPrice(), payload.expiresAt());
            case "BookingConfirmed" -> String.format(
                    "Votre réservation %s pour '%s' est confirmée. Bonne soirée !",
                    aggregateId, payload.eventTitle());
            case "BookingExpired" -> String.format(
                    "Votre réservation %s pour '%s' a expiré faute de paiement.",
                    aggregateId, payload.eventTitle());
            case "BookingCancelled" -> String.format(
                    "Votre réservation %s pour '%s' a été annulée. Remboursement en cours.",
                    aggregateId, payload.eventTitle());
            default -> "Événement " + eventType + " reçu pour la réservation " + aggregateId + ".";
        };
    }
}
