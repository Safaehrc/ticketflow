package com.ticketflow.payment.service;

import com.ticketflow.payment.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC_PAYMENT_SUCCESS = "payment-succeeded-topic";

    public String simulatePayment(String bookingReference, BigDecimal amount, boolean success) {
        String paymentId = UUID.randomUUID().toString();

        if (success) {
            log.info("Paiement réussi simulé pour la réservation : {}", bookingReference);
            PaymentSucceededEvent event = new PaymentSucceededEvent(paymentId, bookingReference, amount, Instant.now());
            kafkaTemplate.send(TOPIC_PAYMENT_SUCCESS, bookingReference, event);
        } else {
            log.warn("Paiement échoué simulé pour la réservation : {}", bookingReference);
        }

        return paymentId;
    }
}