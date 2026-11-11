package com.ticketflow.eventservice.dto;

import java.math.BigDecimal;

public record TicketTierInfo(
        Long id,
        String name,
        BigDecimal price,
        int capacity,
        int availableCapacity) { }
