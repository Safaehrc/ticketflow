package com.ticketflow.bookingservice.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ticketflow.resilience.event-service")
public record EventServiceResilienceProperties(
        @DefaultValue("10") int slidingWindowSize,
        @DefaultValue("5") int minimumNumberOfCalls,
        @DefaultValue("50") float failureRateThreshold,
        @DefaultValue("10s") Duration waitInOpenState,
        @DefaultValue("3") int permittedCallsInHalfOpenState,
        @DefaultValue("4s") Duration timeLimit) { }