package com.ticketflow.bookingservice.clients;

import java.time.Instant;

public record EventResponse(Long id, String title, Instant startsAt) { }