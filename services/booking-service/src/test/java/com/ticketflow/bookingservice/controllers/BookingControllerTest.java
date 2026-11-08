package com.ticketflow.bookingservice.controllers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketflow.bookingservice.clients.EventClient;
import com.ticketflow.bookingservice.clients.EventResponse;
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
}