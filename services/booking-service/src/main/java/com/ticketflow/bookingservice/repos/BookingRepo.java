package com.ticketflow.bookingservice.repos;

import com.ticketflow.bookingservice.entities.Booking;
import com.ticketflow.bookingservice.entities.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerId(Long customerId);
    Optional<Booking> findByBookingReference(String bookingReference);
    Optional<Booking> findByIdempotencyKey(String idempotencyKey);
    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, Instant now);
}
