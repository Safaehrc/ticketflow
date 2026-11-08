package com.ticketflow.eventservice.controllers;

import com.ticketflow.eventservice.dto.*;
import com.ticketflow.eventservice.service.EventQueryService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventQueryService service;

    public EventController(EventQueryService service) {
        this.service = service;
    }

    @GetMapping
    PageResponse<EventSummary> search(
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
    EventDetail get(@PathVariable Long id) {
        return service.get(id);
    }
}