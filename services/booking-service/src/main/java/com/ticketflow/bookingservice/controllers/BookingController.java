package com.ticketflow.bookingservice.controllers;

import com.ticketflow.bookingservice.dto.BookingRequest;
import com.ticketflow.bookingservice.dto.BookingResponse;
import com.ticketflow.bookingservice.entities.Booking;
import com.ticketflow.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request) {
        Booking booking = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/bookings/" + booking.getId()))
                .body(BookingResponse.from(booking));
    }
}