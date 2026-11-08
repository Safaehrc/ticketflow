package com.ticketflow.bookingservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        // 1 s au lieu de 10 s, pour ne pas faire attendre le test
        "ticketflow.resilience.event-service.wait-in-open-state=1s"})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ResilienceTest {

    private enum Mode { UP, DOWN, UNKNOWN_EVENT }

    private static volatile Mode mode = Mode.UP;
    private static final AtomicInteger REQUESTS_RECEIVED = new AtomicInteger();
    private static final HttpServer EVENT_SERVICE = startFakeEventService();

    @Autowired
    MockMvc mockMvc;

    @DynamicPropertySource
    static void eventServiceUrl(DynamicPropertyRegistry registry) {
        registry.add("ticketflow.clients.event-service.url",
                () -> "http://localhost:" + EVENT_SERVICE.getAddress().getPort());
    }

    @BeforeEach
    void healthyEventService() {
        mode = Mode.UP;
        REQUESTS_RECEIVED.set(0);
    }

    @AfterAll
    static void stopFakeEventService() {
        EVENT_SERVICE.stop(0);
    }

    private static HttpServer startFakeEventService() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.setExecutor(Executors.newCachedThreadPool());
            server.createContext("/api/v1/events/", ResilienceTest::handle);
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void handle(HttpExchange exchange) throws IOException {
        REQUESTS_RECEIVED.incrementAndGet();
        String id = exchange.getRequestURI().getPath().substring("/api/v1/events/".length());
        switch (mode) {
            case UP -> reply(exchange, 200,
                    "{\"id\":" + id + ",\"title\":\"Jazz night\",\"startsAt\":\""
                            + Instant.now().plus(30, ChronoUnit.DAYS) + "\"}");
            case DOWN -> reply(exchange, 500, "{\"status\":500}");
            case UNKNOWN_EVENT -> reply(exchange, 404, "{\"status\":404}");
        }
    }

    private static void reply(HttpExchange exchange, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        try {
            exchange.sendResponseHeaders(code, bytes.length);
            exchange.getResponseBody().write(bytes);
        } finally {
            exchange.close();
        }
    }

    private int book(long eventId) throws Exception {
        String body = "{\"eventId\":" + eventId + ",\"quantity\":1}";
        return mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getStatus();
    }

    @Test
    void unknownEventsNeverOpenTheCircuit() throws Exception {
        mode = Mode.UNKNOWN_EVENT;
        for (int i = 0; i < 20; i++) {
            assertThat(book(1)).as("réservation %d d'un événement inconnu", i).isEqualTo(404);
        }
        assertThat(REQUESTS_RECEIVED.get()).as("chaque appel a atteint event-service").isEqualTo(20);

        mode = Mode.UP; // le circuit est resté fermé : ça marche tout de suite
        assertThat(book(2)).isEqualTo(201);
    }

    @Test
    void anOutageOpensTheCircuitThenTheServiceRecovers() throws Exception {
        // 1. event-service en panne : chaque appel l'atteint encore (circuit fermé)
        mode = Mode.DOWN;
        for (int i = 0; i < 5; i++) {
            assertThat(book(3)).as("échec %d", i).isEqualTo(503);
        }
        assertThat(REQUESTS_RECEIVED.get()).isEqualTo(5);

        // 2. 5 appels, 100 % d'échecs : circuit OUVERT, plus aucune requête vers event-service
        for (int i = 0; i < 5; i++) {
            assertThat(book(3)).as("appel %d, circuit ouvert", i).isEqualTo(503);
        }
        assertThat(REQUESTS_RECEIVED.get()).as("aucune requête circuit ouvert").isEqualTo(5);

        // 3. le service revient ; après 1 s le circuit laisse passer 3 appels de test
        mode = Mode.UP;
        Thread.sleep(1300);
        for (int i = 0; i < 3; i++) {
            assertThat(book(4)).as("appel de test %d", i).isEqualTo(201);
        }

        // 4. le circuit est refermé : tout est normal
        for (int i = 0; i < 5; i++) {
            assertThat(book(4)).as("réservation %d après reprise", i).isEqualTo(201);
        }
    }
}