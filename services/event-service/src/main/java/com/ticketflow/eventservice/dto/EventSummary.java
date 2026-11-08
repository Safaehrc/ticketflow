package com.ticketflow.eventservice.dto;

import java.time.Instant;

public record EventSummary(Long id, String title, String summary, Instant startsAt,
                           String city, String venueName, String category) { }