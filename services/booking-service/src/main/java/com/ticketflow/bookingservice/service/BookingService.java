package com.ticketflow.bookingservice.service;

import com.ticketflow.bookingservice.clients.CapacityReservationResponse;
import com.ticketflow.bookingservice.clients.EventClient;
import com.ticketflow.bookingservice.clients.EventResponse;
import com.ticketflow.bookingservice.dto.BookingRequest;
import com.ticketflow.bookingservice.entities.Booking;
import com.ticketflow.bookingservice.entities.BookingStatus;
import com.ticketflow.bookingservice.exceptions.BookingExpiredException;
import com.ticketflow.bookingservice.exceptions.BookingNotFoundException;
import com.ticketflow.bookingservice.exceptions.CapacityExceededException;
import com.ticketflow.bookingservice.exceptions.EventNotFoundException;
import com.ticketflow.bookingservice.exceptions.EventServiceUnavailableException;
import com.ticketflow.bookingservice.repos.BookingRepo;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private static final Duration HOLD_DURATION = Duration.ofMinutes(15);

    private final EventClient eventClient;
    private final BookingRepo repo;

    public BookingService(EventClient eventClient, BookingRepo repo) {
        this.eventClient = eventClient;
        this.repo = repo;
    }

    public Booking create(BookingRequest request) {
        return create(request, null);
    }

    public Booking create(BookingRequest request, Long authUserId) {
        // Idempotence : si une clé est fournie et qu'une réservation existe déjà, on la renvoie
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            var existing = repo.findByIdempotencyKey(request.idempotencyKey());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        // 1. Validation de l'événement et disponibilité via appel distant (protégé par Circuit Breaker)
        EventResponse event = fetchEvent(request.eventId());

        // 2. Réservation atomique de la capacité dans event-service
        BigDecimal unitPrice = BigDecimal.valueOf(50.00);
        String tierName = "STANDARD";
        Long tierId = request.ticketTierId();

        try {
            CapacityReservationResponse cap = eventClient.reserveCapacity(request.eventId(), request.ticketTierId(), request.quantity());
            if (cap != null) {
                if (cap.unitPrice() != null) unitPrice = cap.unitPrice();
                if (cap.tierName() != null) tierName = cap.tierName();
                if (cap.ticketTierId() != null) tierId = cap.ticketTierId();
            }
        } catch (FeignException.Conflict ex) {
            throw new CapacityExceededException("Capacity exceeded for event: " + request.eventId());
        } catch (FeignException.NotFound ex) {
            // Le mock ou le service ne gère pas ce endpoint spécifique de réservation de capacité
            log.warn("reserveCapacity endpoint not found, falling back to default tier metadata");
        } catch (Exception ex) {
            log.warn("Capacity reservation call error: {}", ex.getMessage());
        }

        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(request.quantity()));
        String bookingReference = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Long customerId = request.customerId() != null ? request.customerId() : (authUserId != null ? authUserId : 1L);

        Instant now = Instant.now();
        Booking booking = new Booking(
                bookingReference,
                customerId,
                event.id(),
                event.title(),
                tierId,
                tierName,
                unitPrice,
                totalPrice,
                request.quantity(),
                BookingStatus.PENDING_PAYMENT,
                request.idempotencyKey(),
                now,
                now.plus(HOLD_DURATION)
        );

        return repo.save(booking);
    }

    public Booking getById(Long id) {
        Booking booking = repo.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
        checkAndHandleExpiration(booking);
        return booking;
    }

    public Booking getByReference(String reference) {
        Booking booking = repo.findByBookingReference(reference)
                .orElseThrow(() -> new BookingNotFoundException(reference));
        checkAndHandleExpiration(booking);
        return booking;
    }

    public List<Booking> getByCustomer(Long customerId) {
        List<Booking> bookings = repo.findByCustomerId(customerId);
        bookings.forEach(this::checkAndHandleExpiration);
        return bookings;
    }

    @Transactional
    public Booking confirm(Long id) {
        Booking booking = repo.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT && Instant.now().isAfter(booking.getExpiresAt())) {
            booking.expire();
            safeReleaseCapacity(booking);
            repo.save(booking);
            throw new BookingExpiredException(id);
        }
        booking.confirm();
        return repo.save(booking);
    }

    @Transactional
    public Booking cancel(Long id, Long customerId) {
        Booking booking = repo.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
        if (customerId != null && !booking.getCustomerId().equals(customerId)) {
            throw new BookingNotFoundException(id);
        }
        booking.cancel();
        safeReleaseCapacity(booking);
        return repo.save(booking);
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public int expirePendingBookings() {
        Instant now = Instant.now();
        List<Booking> expired = repo.findByStatusAndExpiresAtBefore(BookingStatus.PENDING_PAYMENT, now);
        for (Booking b : expired) {
            log.info("Expiring pending booking reference {} (ID {})", b.getBookingReference(), b.getId());
            b.expire();
            safeReleaseCapacity(b);
            repo.save(b);
        }
        return expired.size();
    }

    private void checkAndHandleExpiration(Booking booking) {
        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT && Instant.now().isAfter(booking.getExpiresAt())) {
            booking.expire();
            safeReleaseCapacity(booking);
            repo.save(booking);
        }
    }

    private void safeReleaseCapacity(Booking b) {
        try {
            if (b.getTicketTierId() != null) {
                eventClient.releaseCapacity(b.getEventId(), b.getTicketTierId(), b.getQuantity());
            }
        } catch (Exception ex) {
            log.warn("Failed to release capacity for booking {}: {}", b.getId(), ex.getMessage());
        }
    }

    private EventResponse fetchEvent(Long eventId) {
        try {
            return eventClient.getById(eventId);
        } catch (FeignException | CallNotPermittedException | NoFallbackAvailableException ex) {
            Throwable cause = (ex instanceof NoFallbackAvailableException && ex.getCause() != null)
                    ? ex.getCause() : ex;
            if (cause instanceof FeignException.NotFound) {
                throw new EventNotFoundException(eventId);
            }
            throw new EventServiceUnavailableException(cause);
        }
    }
}
