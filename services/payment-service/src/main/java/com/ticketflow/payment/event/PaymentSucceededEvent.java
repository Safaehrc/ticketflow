package com.ticketflow.payment.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSucceededEvent {
    private String paymentId;
    private String bookingReference;
    private BigDecimal amount;
    private Instant timestamp;
}