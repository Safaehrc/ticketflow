package com.ticketflow.payment.controller;

import com.ticketflow.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{bookingReference}/simulate")
    public ResponseEntity<Map<String, Object>> simulatePayment(
            @PathVariable String bookingReference,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "true") boolean success) {

        String paymentId = paymentService.simulatePayment(bookingReference, amount, success);

        return ResponseEntity.ok(Map.of(
                "paymentId", paymentId,
                "bookingReference", bookingReference,
                "status", success ? "PAID" : "FAILED"
        ));
    }
}