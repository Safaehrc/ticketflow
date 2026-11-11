package com.ticketflow.eventservice;

import com.ticketflow.eventservice.entities.*;
import com.ticketflow.eventservice.repos.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
class DataSeeder implements CommandLineRunner {

    private final EventRepo events;
    private final CategoryRepo categories;
    private final VenueRepo venues;
    private final TicketTierRepo tiers;

    DataSeeder(EventRepo events, CategoryRepo categories, VenueRepo venues, TicketTierRepo tiers) {
        this.events = events;
        this.categories = categories;
        this.venues = venues;
        this.tiers = tiers;
    }

    @Override
    public void run(String... args) {
        if (events.count() > 0) {
            return;
        }
        Category concert = categories.save(new Category("Concert"));
        Category theatre = categories.save(new Category("Théâtre"));
        Category sport = categories.save(new Category("Sport"));

        Venue casa = venues.save(new Venue("Grande Salle Démo", "Casablanca", "1 avenue Exemple", 1200));
        Venue rabat = venues.save(new Venue("Théâtre Démo", "Rabat", "2 rue Exemple", 600));
        Venue marrakech = venues.save(new Venue("Stade Démo", "Marrakech", "3 boulevard Exemple", 5000));

        Instant now = Instant.now();
        Event jazz = events.save(new Event("Soirée Jazz de démonstration", "Un quartet jazz pour une soirée.",
                "Description complète de la soirée jazz (données synthétiques).",
                now.plus(Duration.ofDays(10)), now.plus(Duration.ofDays(10)).plus(Duration.ofHours(3)),
                "Organisateur Démo", EventStatus.PUBLISHED, casa, concert));
        tiers.save(new TicketTier("STANDARD", BigDecimal.valueOf(50.00), 200, jazz));
        tiers.save(new TicketTier("VIP", BigDecimal.valueOf(120.00), 50, jazz));

        Event play = events.save(new Event("Pièce de théâtre de démonstration", "Une comédie en trois actes.",
                "Description complète de la pièce (données synthétiques).",
                now.plus(Duration.ofDays(20)), now.plus(Duration.ofDays(20)).plus(Duration.ofHours(2)),
                "Troupe Démo", EventStatus.PUBLISHED, rabat, theatre));
        tiers.save(new TicketTier("STANDARD", BigDecimal.valueOf(35.00), 150, play));

        Event match = events.save(new Event("Match amical de démonstration", "Un match de football amical.",
                "Description complète du match (données synthétiques).",
                now.plus(Duration.ofDays(30)), now.plus(Duration.ofDays(30)).plus(Duration.ofHours(2)),
                "Club Démo", EventStatus.PUBLISHED, marrakech, sport));
        tiers.save(new TicketTier("TRIBUNE", BigDecimal.valueOf(25.00), 1000, match));
        tiers.save(new TicketTier("VIP", BigDecimal.valueOf(90.00), 100, match));

        Event draft = events.save(new Event("Brouillon secret", "Ne doit jamais être visible.",
                "Cet événement est un brouillon.",
                now.plus(Duration.ofDays(40)), null,
                "Organisateur Démo", EventStatus.DRAFT, casa, concert));
        tiers.save(new TicketTier("STANDARD", BigDecimal.valueOf(50.00), 50, draft));
    }
}
