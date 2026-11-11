package com.ticketflow.eventservice.controllers;

import com.ticketflow.eventservice.dto.*;
import com.ticketflow.eventservice.service.CapacityService;
import com.ticketflow.eventservice.service.EventQueryService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventQueryService service;
    private final CapacityService capacityService;

    public EventController(EventQueryService service, CapacityService capacityService) {
        this.service = service;
        this.capacityService = capacityService;
    }

    @GetMapping
    public PageResponse<EventSummary> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.search(q, city, category, from, to, page, size);
    }

    @GetMapping("/{id}")
    public EventDetail get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/{id}/reserve-capacity")
    public CapacityReservationResponse reserveDefaultCapacity(
            @PathVariable Long id,
            @RequestParam(value = "tierId", required = false) Long tierId,
            @RequestParam("quantity") int quantity) {
        return capacityService.reserve(id, tierId, quantity);
    }

    @PostMapping("/{id}/tiers/{tierId}/reserve")
    public CapacityReservationResponse reserveTierCapacity(
            @PathVariable Long id,
            @PathVariable Long tierId,
            @RequestParam("quantity") int quantity) {
        return capacityService.reserve(id, tierId, quantity);
    }

    @PostMapping("/{id}/tiers/{tierId}/release")
    public ResponseEntity<Void> releaseCapacity(
            @PathVariable Long id,
            @PathVariable Long tierId,
            @RequestParam("quantity") int quantity) {
        capacityService.release(id, tierId, quantity);
        return ResponseEntity.noContent().build();
    }
}
