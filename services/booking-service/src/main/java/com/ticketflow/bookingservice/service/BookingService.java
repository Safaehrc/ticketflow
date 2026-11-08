package com.ticketflow.bookingservice.service;

import com.ticketflow.bookingservice.clients.EventClient;
import com.ticketflow.bookingservice.clients.EventResponse;
import com.ticketflow.bookingservice.dto.BookingRequest;
import com.ticketflow.bookingservice.entities.Booking;
import com.ticketflow.bookingservice.entities.BookingStatus;
import com.ticketflow.bookingservice.exceptions.EventNotFoundException;
import com.ticketflow.bookingservice.exceptions.EventServiceUnavailableException;
import com.ticketflow.bookingservice.repos.BookingRepo;
import feign.FeignException;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private static final Duration HOLD_DURATION = Duration.ofMinutes(15);

    private final EventClient eventClient;
    private final BookingRepo repo;

    public BookingService(EventClient eventClient, BookingRepo repo) {
        this.eventClient = eventClient;
        this.repo = repo;
    }

    // Volontairement sans @Transactional : l'appel HTTP distant ne doit pas
    // garder une connexion à la base ouverte pendant qu'il attend.
    public Booking create(BookingRequest request) {
        EventResponse event = fetchEvent(request.eventId());

        Instant now = Instant.now();
        Booking booking = new Booking(event.id(), event.title(), request.quantity(),
                BookingStatus.PENDING_PAYMENT, now, now.plus(HOLD_DURATION));
        return repo.save(booking);
    }

    private EventResponse fetchEvent(Long eventId) {
        try {
            return eventClient.getById(eventId);
        } catch (FeignException.NotFound ex) {
            // Inexistant ou brouillon : même réponse (404), jamais transformée en succès
            throw new EventNotFoundException(eventId);
        } catch (FeignException ex) {
            // Service arrêté, délai dépassé, erreur 5xx...
            throw new EventServiceUnavailableException(ex);
        }
    }
}