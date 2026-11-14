package com.ticketflow.bookingservice.controllers;

import com.ticketflow.bookingservice.events.BookingEvent;
import com.ticketflow.bookingservice.service.KafkaProducerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaTestController {

    private final KafkaProducerService producerService;

    public KafkaTestController(KafkaProducerService producerService) {
        this.producerService = producerService;
    }

    @GetMapping("/test-kafka")
    public String testKafka(@RequestParam(defaultValue = "123") String bookingId) {
        BookingEvent event = new BookingEvent(bookingId, "CREATED", "Test d'envoi asynchrone via Kafka !");
        producerService.sendBookingEvent(event);
        return "Message Kafka bien envoyé pour l'ID : " + bookingId;
    }
}