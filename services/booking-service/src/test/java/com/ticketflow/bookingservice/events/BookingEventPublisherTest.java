package com.ticketflow.bookingservice.events;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketflow.bookingservice.entities.Booking;
import com.ticketflow.bookingservice.entities.BookingStatus;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/**
 * Test d'intégration de BookingEventPublisher avec un broker Kafka embarqué.
 *
 * <p><strong>Architecture du test :</strong></p>
 * <p>
 * On ne charge PAS le contexte Spring Boot complet (trop lourd : OpenFeign, Eureka, etc.).
 * À la place, on utilise {@code @SpringBootTest(classes = ...)} avec une configuration
 * de test minimaliste qui fournit uniquement les beans nécessaires à
 * {@code BookingEventPublisher} : {@code KafkaTemplate} + {@code EmbeddedKafkaBroker}.
 * </p>
 */
@SpringBootTest(classes = {
        BookingEventPublisher.class,
        BookingEventPublisherTest.KafkaTestConfig.class
})
@EmbeddedKafka(
        partitions = 1,
        topics = {"ticketflow.bookings"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@DirtiesContext
@TestPropertySource(properties = {
        "ticketflow.kafka.topics.bookings=ticketflow.bookings",
        "spring.kafka.producer.properties.spring.json.add.type.headers=false"
})
class BookingEventPublisherTest {

    /**
     * Configuration Kafka minimale pour les tests.
     * Fournit le {@code KafkaTemplate<String, Object>} dont {@code BookingEventPublisher}
     * a besoin, en utilisant l'adresse du broker EmbeddedKafka déjà injectée par
     * {@code bootstrapServersProperty}.
     */
    @TestConfiguration
    static class KafkaTestConfig {

        @Bean
        KafkaTemplate<String, Object> kafkaTemplate(EmbeddedKafkaBroker broker) {
            Map<String, Object> producerProps = new HashMap<>();
            producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    broker.getBrokersAsString());
            producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    StringSerializer.class);
            producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    JsonSerializer.class);
            producerProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

            return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));
        }
    }

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private BookingEventPublisher publisher;

    @Test
    void publishesBookingCreatedEvent() {
        // ---- Préparation d'un booking de test ----
        Booking booking = new Booking(
                "BK-TEST1234",
                1L,
                10L,
                "Concert Test",
                1L,
                "VIP",
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(200.00),
                2,
                BookingStatus.PENDING_PAYMENT,
                "idem-key-1",
                Instant.now(),
                Instant.now().plus(Duration.ofMinutes(15))
        );

        // ---- Création d'un consommateur de test ----
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "testGroup-publisher", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (Consumer<String, String> consumer =
                     new DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer()) {

            consumer.subscribe(Collections.singletonList("ticketflow.bookings"));

            // ---- Acte : publication de l'événement ----
            publisher.publishCreated(booking);

            // ---- Vérification ----
            ConsumerRecords<String, String> records =
                    KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));

            assertThat(records.count())
                    .as("Au moins un message doit être publié sur ticketflow.bookings")
                    .isGreaterThanOrEqualTo(1);

            ConsumerRecord<String, String> record = records.iterator().next();
            assertThat(record.key()).isEqualTo("BK-TEST1234");
            assertThat(record.value()).contains("BookingCreated");
            assertThat(record.value()).contains("Concert Test");
        }
    }
}
