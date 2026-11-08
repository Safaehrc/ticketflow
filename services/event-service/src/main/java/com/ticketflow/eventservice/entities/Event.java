package com.ticketflow.eventservice.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(length = 4000)
    private String description;

    @Column(nullable = false)
    private Instant startsAt;

    private Instant endsAt;

    @Column(nullable = false)
    private String organizer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Venue venue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Category category;

    protected Event() { }

    public Event(String title, String summary, String description, Instant startsAt,
                 Instant endsAt, String organizer, EventStatus status,
                 Venue venue, Category category) {
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.organizer = organizer;
        this.status = status;
        this.venue = venue;
        this.category = category;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public String getOrganizer() { return organizer; }
    public EventStatus getStatus() { return status; }
    public Venue getVenue() { return venue; }
    public Category getCategory() { return category; }
}