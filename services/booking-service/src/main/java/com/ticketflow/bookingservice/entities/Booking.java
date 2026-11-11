package com.ticketflow.bookingservice.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bookingReference;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long eventId;

    // Copie du titre au moment de la réservation (le service événements reste l'autorité)
    @Column(nullable = false)
    private String eventTitle;

    private Long ticketTierId;

    private String tierName;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    private String idempotencyKey;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    protected Booking() { }

    public Booking(Long eventId, String eventTitle, int quantity,
                   BookingStatus status, Instant createdAt, Instant expiresAt) {
        this("BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                1L, eventId, eventTitle, null, "STANDARD",
                BigDecimal.valueOf(50.00), BigDecimal.valueOf(50.00 * quantity),
                quantity, status, null, createdAt, expiresAt);
    }

    public Booking(String bookingReference, Long customerId, Long eventId, String eventTitle,
                   Long ticketTierId, String tierName, BigDecimal unitPrice, BigDecimal totalPrice,
                   int quantity, BookingStatus status, String idempotencyKey,
                   Instant createdAt, Instant expiresAt) {
        this.bookingReference = bookingReference;
        this.customerId = customerId;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.ticketTierId = ticketTierId;
        this.tierName = tierName;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.quantity = quantity;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public String getBookingReference() { return bookingReference; }
    public Long getCustomerId() { return customerId; }
    public Long getEventId() { return eventId; }
    public String getEventTitle() { return eventTitle; }
    public Long getTicketTierId() { return ticketTierId; }
    public String getTierName() { return tierName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public int getQuantity() { return quantity; }
    public BookingStatus getStatus() { return status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }

    public void confirm() {
        if (this.status != BookingStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Cannot confirm booking in state: " + this.status);
        }
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status != BookingStatus.PENDING_PAYMENT && this.status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel booking in state: " + this.status);
        }
        this.status = BookingStatus.CANCELLED;
    }

    public void expire() {
        this.status = BookingStatus.EXPIRED;
    }
}
