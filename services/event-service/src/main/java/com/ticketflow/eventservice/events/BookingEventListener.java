package com.ticketflow.eventservice.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketflow.eventservice.service.CapacityService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventListener {

    private final CapacityService capacityService;
    private final ObjectMapper objectMapper;

    public BookingEventListener(CapacityService capacityService, ObjectMapper objectMapper) {
        this.capacityService = capacityService;
        this.objectMapper = objectMapper;
    }

    // On écoute le topic Kafka où le booking-service publiera ses annulations
    @KafkaListener(topics = "booking-cancelled-topic", groupId = "event-capacity-group")
    public void handleBookingCancelled(String message) {
        try {
            // 1. Lecture et extraction des données du message JSON
            JsonNode jsonNode = objectMapper.readTree(message);
            Long eventId = jsonNode.get("eventId").asLong();
            Long tierId = jsonNode.get("tierId").asLong();
            int quantityToRelease = jsonNode.get("quantity").asInt();

            // 2. Appel de votre méthode métier sécurisée pour restituer le stock
            capacityService.release(eventId, tierId, quantityToRelease);

            System.out.println("✅ Capacité restituée avec succès pour l'événement : " + eventId);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la lecture du message Kafka : " + e.getMessage());
        }
    }
}