package com.ticketflow.bookingservice.service;

import com.ticketflow.bookingservice.events.BookingEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "booking-events";

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendBookingEvent(BookingEvent event) {
        kafkaTemplate.send(TOPIC, event.bookingId(), event);
        System.out.println(" [Kafka] Événement envoyé avec succès : " + event);
    }
}