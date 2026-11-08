package com.ticketflow.bookingservice.repos;

import com.ticketflow.bookingservice.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepo extends JpaRepository<Booking, Long> { }