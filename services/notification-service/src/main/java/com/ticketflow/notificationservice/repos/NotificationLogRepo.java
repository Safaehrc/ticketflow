package com.ticketflow.notificationservice.repos;

import com.ticketflow.notificationservice.entities.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Dépôt JPA pour la table {@code notification_log}.
 */
public interface NotificationLogRepo extends JpaRepository<NotificationLog, Long> {

    /**
     * Recherche un enregistrement par l'identifiant d'événement de domaine.
     * Utilisé pour la déduplication idempotente des messages Kafka.
     *
     * @param eventId UUID de l'événement de domaine
     * @return l'enregistrement s'il existe
     */
    Optional<NotificationLog> findByEventId(String eventId);
}
