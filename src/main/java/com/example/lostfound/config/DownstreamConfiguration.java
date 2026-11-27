package com.example.lostfound.config;

import com.example.lostfound.client.DownstreamClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DownstreamConfiguration {
    @Bean
    DownstreamClient downstreamClient(DownstreamProperties properties) {
        return new DownstreamClient(properties.baseUrl(), properties.timeout());
    }
}
