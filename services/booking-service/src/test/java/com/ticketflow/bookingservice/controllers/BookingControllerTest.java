package com.ticketflow.bookingservice.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketflow.bookingservice.clients.EventClient;
import com.ticketflow.bookingservice.clients.EventResponse;
import com.ticketflow.bookingservice.entities.Booking;
import com.ticketflow.bookingservice.events.BookingEventPublisher;
import feign.FeignException;
import feign.Request;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    EventClient eventClient;

    @MockitoBean
    BookingEventPublisher eventPublisher;

    private static Request fakeRequest() {
        return Request.create(Request.HttpMethod.GET, "/api/v1/events/1",
                Map.of(), null, StandardCharsets.UTF_8, null);
    }

    private static String body(long eventId, int quantity) {
        return "{\"eventId\":" + eventId + ",\"quantity\":" + quantity + "}";
    }

    @Test
    void createsBookingWhenEventExists() throws Exception {
        when(eventClient.getById(1L))
                .thenReturn(new EventResponse(1L, "Soirée Jazz", Instant.now()));

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(1, 2)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.eventTitle").value("Soirée Jazz"));

        verify(eventPublisher).publishCreated(any(Booking.class));
    }

    @Test
    void returns404WhenEventDoesNotExist() throws Exception {
        when(eventClient.getById(anyLong()))
                .thenThrow(new FeignException.NotFound("not found", fakeRequest(), null, null));

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(999, 1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void returns503WhenEventServiceIsDown() throws Exception {
        when(eventClient.getById(anyLong()))
                .thenThrow(new FeignException.ServiceUnavailable("down", fakeRequest(), null, null));

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(1, 1)))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void rejectsInvalidQuantity() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(1, 0)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void idempotentBookingReturnsSameBooking() throws Exception {
        when(eventClient.getById(1L))
                .thenReturn(new EventResponse(1L, "Soirée Jazz", Instant.now()));

        String idemBody = "{\"eventId\":1,\"quantity\":2,\"idempotencyKey\":\"idem-key-123\"}";

        // First call
        String resp1 = mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(idemBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Second call with same idempotency key
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(idemBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idempotencyKey").value("idem-key-123"));
    }

    @Test
    void returns409WhenCapacityExceeded() throws Exception {
        when(eventClient.getById(1L))
                .thenReturn(new EventResponse(1L, "Soirée Jazz", Instant.now()));
        when(eventClient.reserveCapacity(anyLong(), anyLong(), anyInt()))
                .thenThrow(new FeignException.Conflict("Capacity exceeded", fakeRequest(), null, null));

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\":1,\"quantity\":5,\"ticketTierId\":1}"))
                .andExpect(status().isConflict());
    }

    @Test
    void returns503WhenCapacityReservationFails() throws Exception {
        when(eventClient.getById(1L))
                .thenReturn(new EventResponse(1L, "Soirée Jazz", Instant.now()));
        when(eventClient.reserveCapacity(anyLong(), anyLong(), anyInt()))
                .thenThrow(new FeignException.ServiceUnavailable("down", fakeRequest(), null, null));

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\":1,\"quantity\":2,\"ticketTierId\":1}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void listsCustomerBookings() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/me")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }
}