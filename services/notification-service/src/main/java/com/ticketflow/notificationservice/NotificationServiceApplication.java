package com.ticketflow.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Point d'entrée du notification-service.
 * <p>
 * Ce microservice écoute les événements de réservation publiés sur le bus Kafka
 * (topic {@code ticketflow.bookings}) et simule l'envoi de notifications
 * (email, SMS, Telegram) en les persistant dans une table {@code notification_log}.
 * </p>
 */
@SpringBootApplication
@EnableScheduling
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
