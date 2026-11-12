package com.ticketflow.eventservice.repos;

import com.ticketflow.eventservice.entities.TicketTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface TicketTierRepo extends JpaRepository<TicketTier, Long> {

    // Verrouille la ligne en base de données pour éviter les conflits de concurrence
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketTier t WHERE t.id = :id")
    Optional<TicketTier> findByIdLocked(@Param("id") Long id);
}