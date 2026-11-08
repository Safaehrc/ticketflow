package com.ticketflow.eventservice.repos;

import com.ticketflow.eventservice.entities.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface EventRepo extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    // Charge lieu et catégorie en une seule requête (évite une requête par ligne)
    @Override
    @EntityGraph(attributePaths = {"venue", "category"})
    Page<Event> findAll(Specification<Event> spec, Pageable pageable);
}
