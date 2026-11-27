package com.example.lostfound.config;

import com.example.lostfound.client.DownstreamClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("dev")
class DevProfileTest {
    @Autowired
    private DownstreamProperties downstream;

    @Autowired
    private DownstreamClient client;

    @Test
    void devProfileBindsDownstreamConfiguration() {
        assertEquals(URI.create("http://localhost:9090"), downstream.baseUrl());
        assertEquals(Duration.ofSeconds(2), downstream.timeout());
        assertEquals(downstream.baseUrl(), client.baseUrl());
        assertEquals(downstream.timeout(), client.timeout());
    }
}
