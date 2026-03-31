package com.ruslandontsov.fitness.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenRouterConfig {

    @Bean
    RestClient openRouterRestClient() {
        return RestClient.builder()
                .baseUrl("https://openrouter.ai/api/v1")
                .build();
    }
}