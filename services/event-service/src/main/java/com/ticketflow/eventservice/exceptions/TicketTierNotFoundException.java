package com.ticketflow.eventservice.exceptions;

public class TicketTierNotFoundException extends RuntimeException {
    public TicketTierNotFoundException(Long tierId) {
        super("Ticket tier not found: " + tierId);
    }
}
