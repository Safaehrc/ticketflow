package com.ticketflow.bookingservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Pas d'attribut "url" : le nom est résolu par Eureka + LoadBalancer
@FeignClient(name = "event-service", url = "${ticketflow.clients.event-service.url:}")
public interface EventClient {

    @GetMapping("/api/v1/events/{id}")
    EventResponse getById(@PathVariable("id") Long id);

    @PostMapping("/api/v1/events/{id}/reserve-capacity")
    CapacityReservationResponse reserveCapacity(
            @PathVariable("id") Long id,
            @RequestParam(value = "tierId", required = false) Long tierId,
            @RequestParam("quantity") int quantity);

    @PostMapping("/api/v1/events/{id}/tiers/{tierId}/release")
    void releaseCapacity(
            @PathVariable("id") Long id,
            @PathVariable("tierId") Long tierId,
            @RequestParam("quantity") int quantity);
}
