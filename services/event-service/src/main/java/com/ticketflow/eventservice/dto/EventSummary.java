package com.ticketflow.eventservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record EventSummary(Long id, String title, String summary, Instant startsAt,
                           String city, String venueName, String category,
                           BigDecimal startingPrice, boolean available) {

    public EventSummary(Long id, String title, String summary, Instant startsAt,
                        String city, String venueName, String category) {
        this(id, title, summary, startsAt, city, venueName, category, null, true);
    }
}
