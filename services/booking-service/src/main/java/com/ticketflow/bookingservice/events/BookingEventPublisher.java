package com.ticketflow.bookingservice.events;

import com.ticketflow.bookingservice.entities.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BookingEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public BookingEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${ticketflow.kafka.topics.bookings:ticketflow.bookings}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishCreated(Booking booking) {
        publish("BookingCreated", booking);
    }

    public void publishConfirmed(Booking booking) {
        publish("BookingConfirmed", booking);
    }

    public void publishExpired(Booking booking) {
        publish("BookingExpired", booking);
    }

    public void publishCancelled(Booking booking) {
        publish("BookingCancelled", booking);
    }

    private void publish(String eventType, Booking booking) {
        try {
            DomainEvent<BookingEventPayload> event = DomainEvent.of(
                    eventType,
                    booking.getBookingReference(),
                    BookingEventPayload.from(booking)
            );
            log.info("Publishing {} event for booking reference: {}", eventType, booking.getBookingReference());
            kafkaTemplate.send(topic, booking.getBookingReference(), event);
        } catch (Exception ex) {
            log.error("Failed to publish {} event for booking reference {}: {}",
                    eventType, booking.getBookingReference(), ex.getMessage());
        }
    }
}
