package com.ticketflow.eventservice.service;

import com.ticketflow.eventservice.dto.CapacityReservationResponse;
import com.ticketflow.eventservice.entities.Event;
import com.ticketflow.eventservice.entities.TicketTier;
import com.ticketflow.eventservice.exceptions.CapacityExceededException;
import com.ticketflow.eventservice.exceptions.TicketTierNotFoundException;
import com.ticketflow.eventservice.repos.EventRepo;
import com.ticketflow.eventservice.repos.TicketTierRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CapacityService {

    private final EventRepo eventRepo;
    private final TicketTierRepo tierRepo;

    public CapacityService(EventRepo eventRepo, TicketTierRepo tierRepo) {
        this.eventRepo = eventRepo;
        this.tierRepo = tierRepo;
    }

    private TicketTier resolveTier(Event event, Long tierId) {
        Long resolvedTierId = tierId;
        if (resolvedTierId == null) {
            final Long firstTierId = event.getTicketTiers()
                    .stream()
                    .findFirst()
                    .map(TicketTier::getId)
                    .orElseThrow(() -> new TicketTierNotFoundException(null));
            resolvedTierId = firstTierId;
        }

        final Long targetTierId = resolvedTierId;
        return tierRepo.findByIdLocked(targetTierId)
                .orElseThrow(() -> new TicketTierNotFoundException(targetTierId));
    }

    @Transactional
    public void release(Long eventId, Long tierId, int quantityToRelease) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        TicketTier tier = resolveTier(event, tierId);
        tier.release(quantityToRelease);
        tier.setAvailableQuantity(tier.getAvailableCapacity());
        tierRepo.save(tier);
    }

    @Transactional
    public CapacityReservationResponse reserve(Long eventId, Long tierId, int quantityToReserve) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));

        TicketTier tier = resolveTier(event, tierId);

        if (quantityToReserve <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (tier.getAvailableCapacity() < quantityToReserve) {
            throw new CapacityExceededException("Not enough capacity available for this tier");
        }

        tier.reserve(quantityToReserve);
        tier.setAvailableQuantity(tier.getAvailableCapacity());
        tierRepo.save(tier);

        return new CapacityReservationResponse(
                event.getId(),
                event.getTitle(),
                tier.getId(),
                tier.getName(),
                tier.getPrice(),
                quantityToReserve,
                tier.getAvailableCapacity()
        );
    }
}