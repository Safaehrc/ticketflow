package com.ticketflow.notificationservice.controllers;

import com.ticketflow.notificationservice.entities.NotificationLog;
import com.ticketflow.notificationservice.repos.NotificationLogRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST d'administration des notifications.
 * <p>
 * Endpoints disponibles :
 * <ul>
 *   <li>GET  /api/notifications              — liste toutes les notifications</li>
 *   <li>GET  /api/notifications/{id}         — récupère une notification par ID</li>
 *   <li>GET  /api/notifications/event/{eventId} — récupère par eventId (déduplication)</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationLogRepo repo;

    public NotificationController(NotificationLogRepo repo) {
        this.repo = repo;
    }

    /** Liste toutes les notifications enregistrées. */
    @GetMapping
    public List<NotificationLog> findAll() {
        return repo.findAll();
    }

    /** Récupère une notification par son ID interne. */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationLog> findById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Récupère une notification par son eventId (UUID de l'événement de domaine). */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<NotificationLog> findByEventId(@PathVariable String eventId) {
        return repo.findByEventId(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
