package com.ticketflow.eventservice.service;

import com.ticketflow.eventservice.dto.CapacityReservationResponse;
import com.ticketflow.eventservice.entities.TicketTier;
import com.ticketflow.eventservice.repos.EventRepo;
import com.ticketflow.eventservice.repos.TicketTierRepo;
import com.ticketflow.eventservice.entities.Event;
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

    @Transactional
    public void release(Long eventId, Long tierId, int quantityToRelease) {
        eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        TicketTier tier = tierRepo.findByIdLocked(tierId)
                .orElseThrow(() -> new RuntimeException("Tier not found"));

        int newAvailableCapacity = tier.getAvailableQuantity() + quantityToRelease;
        tier.setAvailableQuantity(newAvailableCapacity);

        tierRepo.save(tier);
    }

    @Transactional
    public CapacityReservationResponse reserve(Long eventId, Long tierId, int quantityToReserve) {
        // 1. Vérification et récupération de l'événement (assigné à une variable 'event')
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));

        // 2. Verrouillage de la catégorie
        TicketTier tier = tierRepo.findByIdLocked(tierId)
                .orElseThrow(() -> new RuntimeException("Ticket tier not found with ID: " + tierId));

        // 3. Vérification du stock disponible
        if (tier.getAvailableQuantity() < quantityToReserve) {
            throw new RuntimeException("Not enough capacity available for this tier");
        }

        // 4. Déduction et sauvegarde
        int newAvailableCapacity = tier.getAvailableQuantity() - quantityToReserve;
        tier.setAvailableQuantity(newAvailableCapacity);
        tierRepo.save(tier);

        // 5. Retour de l'objet complet attendu par le contrôleur
        // NOTE : Vérifiez que les méthodes getTitle(), getName() et getPrice() correspondent bien
        // aux noms dans vos entités Event et TicketTier.
        return new CapacityReservationResponse(
                event.getId(),
                event.getTitle(), // ou event.getName() selon votre entité
                tier.getId(),
                tier.getName(),
                tier.getPrice(),  // ou tier.getUnitPrice()
                quantityToReserve,
                newAvailableCapacity
        );
    }
}