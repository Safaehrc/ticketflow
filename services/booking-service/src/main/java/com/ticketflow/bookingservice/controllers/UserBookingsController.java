package com.ticketflow.bookingservice.controllers;

import com.ticketflow.bookingservice.dto.BookingResponse;
import com.ticketflow.bookingservice.service.BookingService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/bookings")
public class UserBookingsController {

    private final BookingService service;

    public UserBookingsController(BookingService service) {
        this.service = service;
    }

    @GetMapping
    public List<BookingResponse> listMyBookings(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long customerId) {
        return service.getByCustomer(customerId).stream().map(BookingResponse::from).toList();
    }
}
