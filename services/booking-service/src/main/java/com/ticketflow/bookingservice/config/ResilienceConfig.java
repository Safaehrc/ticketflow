package com.ticketflow.bookingservice.config;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EventServiceResilienceProperties.class)
public class ResilienceConfig {

    @Bean
    Customizer<Resilience4JCircuitBreakerFactory> eventServiceCircuitBreaker(
            EventServiceResilienceProperties p) {

        CircuitBreakerConfig breaker = CircuitBreakerConfig.custom()
                .slidingWindowSize(p.slidingWindowSize())
                .minimumNumberOfCalls(p.minimumNumberOfCalls())
                .failureRateThreshold(p.failureRateThreshold())
                .waitDurationInOpenState(p.waitInOpenState())
                .permittedNumberOfCallsInHalfOpenState(p.permittedCallsInHalfOpenState())
                // 404 et 400 sont des réponses normales d'un service en bonne santé :
                // ni échec, ni succès, ils ne font pas monter le taux d'échec
                .ignoreExceptions(FeignException.NotFound.class, FeignException.BadRequest.class)
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .build();

        TimeLimiterConfig limiter = TimeLimiterConfig.custom()
                .timeoutDuration(p.timeLimit())
                .build();

        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(breaker)
                .timeLimiterConfig(limiter)
                .build());
    }
}