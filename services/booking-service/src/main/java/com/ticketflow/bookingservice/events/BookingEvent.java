package com.ticketflow.bookingservice.events;

public record BookingEvent(String bookingId, String status, String message) {}