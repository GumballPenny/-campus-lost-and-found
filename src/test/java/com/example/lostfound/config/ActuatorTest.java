package com.example.lostfound.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class ActuatorTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Test
    void healthIsUpAndSensitiveEndpointsAreHidden() {
        ResponseEntity<String> health = rest.getForEntity(
                "http://localhost:" + port + "/actuator/health",
                String.class
        );
        assertEquals(HttpStatus.OK, health.getStatusCode());
        assertTrue(health.getBody().contains("\"status\":\"UP\""));

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                rest.getForEntity("http://localhost:" + port + "/actuator/env", String.class).getStatusCode()
        );
        assertEquals(
                HttpStatus.UNAUTHORIZED,
                rest.getForEntity("http://localhost:" + port + "/actuator/heapdump", String.class).getStatusCode()
        );
    }
}
