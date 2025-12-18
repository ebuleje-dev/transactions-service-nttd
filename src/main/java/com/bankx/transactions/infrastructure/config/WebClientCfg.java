package com.bankx.transactions.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientCfg {

    @Bean
    public WebClient riskWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8084/mock/risk")
                .build();
    }
}
