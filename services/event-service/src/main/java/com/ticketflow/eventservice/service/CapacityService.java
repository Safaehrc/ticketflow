package com.ticketflow.eventservice.service;

import com.ticketflow.eventservice.dto.CapacityReservationResponse;
import com.ticketflow.eventservice.entities.Event;
import com.ticketflow.eventservice.entities.EventStatus;
import com.ticketflow.eventservice.entities.TicketTier;
import com.ticketflow.eventservice.exceptions.CapacityExceededException;
import com.ticketflow.eventservice.exceptions.EventNotFoundException;
import com.ticketflow.eventservice.exceptions.TicketTierNotFoundException;
import com.ticketflow.eventservice.repos.EventRepo;
import com.ticketflow.eventservice.repos.TicketTierRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CapacityService {

    private final EventRepo eventRepo;
    private final TicketTierRepo tierRepo;

    public CapacityService(EventRepo eventRepo, TicketTierRepo tierRepo) {
        this.eventRepo = eventRepo;
        this.tierRepo = tierRepo;
    }

    public CapacityReservationResponse reserve(Long eventId, Long tierId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Event event = eventRepo.findById(eventId)
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        TicketTier tier;
        if (tierId != null) {
            tier = tierRepo.findById(tierId)
                    .filter(t -> t.getEvent().getId().equals(eventId))
                    .orElseThrow(() -> new TicketTierNotFoundException(tierId));
        } else {
            // Si le tier n'est pas spécifié, on choisit le premier tier disponible ayant assez de capacité
            tier = event.getTicketTiers().stream()
                    .filter(t -> t.getAvailableCapacity() >= quantity)
                    .findFirst()
                    .orElseGet(() -> event.getTicketTiers().isEmpty() ? null : event.getTicketTiers().get(0));
        }

        if (tier == null) {
            throw new CapacityExceededException("No available ticket tier for event " + eventId);
        }

        if (tier.getAvailableCapacity() < quantity) {
            throw new CapacityExceededException("Requested " + quantity + " seats, only " + tier.getAvailableCapacity() + " remaining");
        }

        tier.reserve(quantity);
        tierRepo.save(tier);

        return new CapacityReservationResponse(
                event.getId(),
                event.getTitle(),
                tier.getId(),
                tier.getName(),
                tier.getPrice(),
                quantity,
                tier.getAvailableCapacity()
        );
    }

    public void release(Long eventId, Long tierId, int quantity) {
        if (quantity <= 0) return;

        if (tierId != null) {
            tierRepo.findById(tierId).ifPresent(tier -> {
                tier.release(quantity);
                tierRepo.save(tier);
            });
        } else {
            eventRepo.findById(eventId).ifPresent(event -> {
                if (!event.getTicketTiers().isEmpty()) {
                    TicketTier first = event.getTicketTiers().get(0);
                    first.release(quantity);
                    tierRepo.save(first);
                }
            });
        }
    }
}
