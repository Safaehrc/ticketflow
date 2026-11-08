package com.ticketflow.eventservice.service;

import com.ticketflow.eventservice.dto.*;
import com.ticketflow.eventservice.entities.Event;
import com.ticketflow.eventservice.entities.EventStatus;
import com.ticketflow.eventservice.exceptions.EventNotFoundException;
import com.ticketflow.eventservice.repos.EventRepo;
import com.ticketflow.eventservice.repos.EventSpecs;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.util.StringUtils.hasText;

@Service
@Transactional(readOnly = true)
public class EventQueryService {

    private static final int MAX_PAGE_SIZE = 50;

    private final EventRepo repo;

    public EventQueryService(EventRepo repo) {
        this.repo = repo;
    }

    public PageResponse<EventSummary> search(String q, String city, String category,
                                             LocalDate from, LocalDate to,
                                             int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        // Seuls les événements publiés sont visibles publiquement
        Specification<Event> spec = EventSpecs.published();
        if (hasText(q)) spec = spec.and(EventSpecs.titleContains(q.trim()));
        if (hasText(city)) spec = spec.and(EventSpecs.inCity(city.trim()));
        if (hasText(category)) spec = spec.and(EventSpecs.inCategory(category.trim()));
        if (from != null) {
            spec = spec.and(EventSpecs.startsFrom(from.atStartOfDay(ZoneOffset.UTC).toInstant()));
        }
        if (to != null) {
            spec = spec.and(EventSpecs.startsBefore(to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()));
        }

        // Tri fixe : l'appelant ne choisit pas le tri
        PageRequest pageRequest = PageRequest.of(safePage, safeSize,
                Sort.by("startsAt").ascending().and(Sort.by("id").ascending()));
        Page<Event> result = repo.findAll(spec, pageRequest);

        return new PageResponse<>(
                result.getContent().stream().map(this::toSummary).toList(),
                result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    public EventDetail get(Long id) {
        // Un brouillon répond 404, comme un événement inexistant : pas de fuite
        Event event = repo.findById(id)
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(id));
        return new EventDetail(event.getId(), event.getTitle(), event.getSummary(),
                event.getDescription(), event.getStartsAt(), event.getEndsAt(),
                event.getOrganizer(),
                new VenueInfo(event.getVenue().getName(), event.getVenue().getCity(),
                        event.getVenue().getAddress(), event.getVenue().getCapacity()),
                event.getCategory().getName());
    }

    private EventSummary toSummary(Event e) {
        return new EventSummary(e.getId(), e.getTitle(), e.getSummary(), e.getStartsAt(),
                e.getVenue().getCity(), e.getVenue().getName(), e.getCategory().getName());
    }
}