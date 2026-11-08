package com.ticketflow.eventservice.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketflow.eventservice.entities.Event;
import com.ticketflow.eventservice.entities.EventStatus;
import com.ticketflow.eventservice.repos.EventRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EventRepo repo;

    @Test
    void listContainsOnlyPublishedEvents() throws Exception {
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[?(@.title == 'Brouillon secret')]").isEmpty());
    }

    @Test
    void filtersByCity() throws Exception {
        mockMvc.perform(get("/api/v1/events").param("city", "Rabat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].city").value("Rabat"));
    }

    @Test
    void searchesByTitleIgnoringCase() throws Exception {
        mockMvc.perform(get("/api/v1/events").param("q", "JAZZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void pageSizeIsCapped() throws Exception {
        mockMvc.perform(get("/api/v1/events").param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(50));
    }

    @Test
    void publishedEventDetailIsReturned() throws Exception {
        Long id = repo.findAll().stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .map(Event::getId).findFirst().orElseThrow();

        mockMvc.perform(get("/api/v1/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venue.city").exists());
    }

    @Test
    void draftEventIsNeverExposed() throws Exception {
        Long draftId = repo.findAll().stream()
                .filter(e -> e.getStatus() == EventStatus.DRAFT)
                .map(Event::getId).findFirst().orElseThrow();

        mockMvc.perform(get("/api/v1/events/{id}", draftId))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownEventIsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/events/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}