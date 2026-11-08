package com.ticketflow.eventservice.repos;

import com.ticketflow.eventservice.entities.Event;
import com.ticketflow.eventservice.entities.EventStatus;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

public final class EventSpecs {

    private EventSpecs() { }

    public static Specification<Event> published() {
        return (root, query, cb) -> cb.equal(root.get("status"), EventStatus.PUBLISHED);
    }

    public static Specification<Event> titleContains(String text) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + text.toLowerCase() + "%");
    }

    public static Specification<Event> inCity(String city) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("venue").get("city")), city.toLowerCase());
    }

    public static Specification<Event> inCategory(String category) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("category").get("name")), category.toLowerCase());
    }

    public static Specification<Event> startsFrom(Instant from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startsAt"), from);
    }

    public static Specification<Event> startsBefore(Instant to) {
        return (root, query, cb) -> cb.lessThan(root.get("startsAt"), to);
    }
}