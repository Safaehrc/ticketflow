package com.ticketflow.bookingservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Pas d'attribut "url" : le nom est résolu par Eureka + LoadBalancer
@FeignClient(name = "event-service")
public interface EventClient {

    @GetMapping("/api/v1/events/{id}")
    EventResponse getById(@PathVariable("id") Long id);
}