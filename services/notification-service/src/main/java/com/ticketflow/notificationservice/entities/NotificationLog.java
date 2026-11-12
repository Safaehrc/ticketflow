package com.ticketflow.notificationservice.entities;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Enregistrement persistant d'une notification envoyée (ou échouée).
 * <p>
 * Le champ {@code eventId} sert de clé de déduplication idempotente :
 * si un message Kafka est consommé deux fois (au-moins-une-fois), on évite
 * d'envoyer la même notification en double.
 * </p>
 */
@Entity
@Table(name = "notification_log",
       indexes = @Index(name = "idx_notification_event_id", columnList = "eventId", unique = true))
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifiant unique de l'événement de domaine (UUID).
     * Utilisé pour la déduplication idempotente.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String eventId;

    /**
     * Référence de la réservation concernée (ex: BK-XXXXXXXX).
     */
    @Column(nullable = false, length = 32)
    private String aggregateId;

    /**
     * Destinataire de la notification (customerId sous forme de String).
     */
    @Column(nullable = false, length = 128)
    private String recipient;

    /** Canal utilisé pour envoyer la notification. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private NotificationChannel channel;

    /** Objet / titre de la notification. */
    @Column(nullable = false, length = 256)
    private String subject;

    /** Contenu complet du message. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Résultat de l'envoi. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private NotificationStatus status;

    /** Horodatage de création de cet enregistrement. */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected NotificationLog() {}

    public NotificationLog(String eventId, String aggregateId, String recipient,
                           NotificationChannel channel, String subject, String content,
                           NotificationStatus status) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.recipient = recipient;
        this.channel = channel;
        this.subject = subject;
        this.content = content;
        this.status = status;
        this.createdAt = Instant.now();
    }

    // ---- Getters ----

    public Long getId()                     { return id; }
    public String getEventId()              { return eventId; }
    public String getAggregateId()          { return aggregateId; }
    public String getRecipient()            { return recipient; }
    public NotificationChannel getChannel() { return channel; }
    public String getSubject()              { return subject; }
    public String getContent()              { return content; }
    public NotificationStatus getStatus()   { return status; }
    public Instant getCreatedAt()           { return createdAt; }
}
