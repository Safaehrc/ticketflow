package com.ticketflow.eventservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ticket_tiers")
public class TicketTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int availableCapacity;
    private int availableQuantity;

    @Version
    private Long version;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    protected TicketTier() { }

    public TicketTier(String name, BigDecimal price, int capacity, Event event) {
        this.name = name;
        this.price = price;
        this.capacity = capacity;
        this.availableCapacity = capacity;
        this.availableQuantity = capacity;
        this.event = event;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getCapacity() { return capacity; }
    public int getAvailableCapacity() { return availableCapacity; }
    public Event getEvent() { return event; }
    public Long getVersion() { return version; }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (this.availableCapacity < quantity) {
            throw new IllegalStateException("Capacity exceeded: requested " + quantity + " but only " + this.availableCapacity + " available");
        }
        this.availableCapacity -= quantity;
    }

    public void release(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.availableCapacity = Math.min(this.capacity, this.availableCapacity + quantity);
    }
    public int getAvailableQuantity() {
        return this.availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}
