package com.ticketflow.notificationservice.entities;

/**
 * Statut d'envoi d'une notification.
 */
public enum NotificationStatus {
    /** La notification a été simulée/envoyée avec succès. */
    SENT,
    /** L'envoi a échoué (après plusieurs tentatives). */
    FAILED
}
