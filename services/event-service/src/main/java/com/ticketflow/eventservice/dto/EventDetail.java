package com.ticketflow.eventservice.dto;

import java.time.Instant;
import java.util.List;

public record EventDetail(Long id, String title, String summary, String description,
                          Instant startsAt, Instant endsAt, String organizer,
                          VenueInfo venue, String category,
                          List<TicketTierInfo> ticketTiers) {

    public EventDetail(Long id, String title, String summary, String description,
                       Instant startsAt, Instant endsAt, String organizer,
                       VenueInfo venue, String category) {
        this(id, title, summary, description, startsAt, endsAt, organizer, venue, category, List.of());
    }
}
