package com.bankx.transactions.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for WebClient beans.
 * Provides configured WebClient instances for external service calls.
 */
@Configuration
public class WebClientCfg {

  /**
   * Creates a WebClient bean for risk service communication.
   *
   * @return configured WebClient pointing to risk service endpoint
   */
  @Bean
  public WebClient riskWebClient() {
    return WebClient.builder()
        .baseUrl("http://localhost:8084/mock/risk")
        .build();
  }
}
