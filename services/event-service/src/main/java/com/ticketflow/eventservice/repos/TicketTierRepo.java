package com.ticketflow.eventservice.repos;

import com.ticketflow.eventservice.entities.TicketTier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketTierRepo extends JpaRepository<TicketTier, Long> {
    List<TicketTier> findByEventId(Long eventId);
}
