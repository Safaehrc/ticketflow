package com.ticketflow.eventservice.dto;

import java.time.Instant;

public record EventDetail(Long id, String title, String summary, String description,
                          Instant startsAt, Instant endsAt, String organizer,
                          VenueInfo venue, String category) { }