package com.ticketflow.bookingservice.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    // Copie du titre au moment de la réservation (le service événements reste l'autorité)
    @Column(nullable = false)
    private String eventTitle;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    protected Booking() { }

    public Booking(Long eventId, String eventTitle, int quantity,
                   BookingStatus status, Instant createdAt, Instant expiresAt) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public String getEventTitle() { return eventTitle; }
    public int getQuantity() { return quantity; }
    public BookingStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
}