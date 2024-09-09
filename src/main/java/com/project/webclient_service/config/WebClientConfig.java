package com.project.webclient_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${main.url}")
    private String mainUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.create(mainUrl);
    }
}


