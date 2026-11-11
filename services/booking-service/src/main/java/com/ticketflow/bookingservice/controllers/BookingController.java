package com.ticketflow.bookingservice.controllers;

import com.ticketflow.bookingservice.dto.BookingRequest;
import com.ticketflow.bookingservice.dto.BookingResponse;
import com.ticketflow.bookingservice.entities.Booking;
import com.ticketflow.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
    public ResponseEntity<BookingResponse> create(
            @Valid @RequestBody BookingRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long authUserId) {
        Booking booking = service.create(request, authUserId);
        return ResponseEntity.created(URI.create("/api/v1/bookings/" + booking.getId()))
                .body(BookingResponse.from(booking));
    }

    @GetMapping("/{id}")
    public BookingResponse getById(@PathVariable Long id) {
        return BookingResponse.from(service.getById(id));
    }

    @GetMapping("/reference/{reference}")
    public BookingResponse getByReference(@PathVariable String reference) {
        return BookingResponse.from(service.getByReference(reference));
    }

    @GetMapping("/me")
    public List<BookingResponse> getMyBookings(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long customerId) {
        return service.getByCustomer(customerId).stream().map(BookingResponse::from).toList();
    }

    @PostMapping("/{id}/confirm")
    public BookingResponse confirm(@PathVariable Long id) {
        return BookingResponse.from(service.confirm(id));
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long customerId) {
        return BookingResponse.from(service.cancel(id, customerId));
    }
}
