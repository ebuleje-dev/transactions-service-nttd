package com.bankx.transactions.infrastructure.web.filter;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Web filter for correlation ID propagation.
 * Extracts or generates correlation ID and adds it to reactive context.
 */
@Component
public class CorrelationFilter implements WebFilter {
  private static final String HEADER = "X-Correlation-Id";

  /**
   * Filters incoming requests to extract or generate correlation ID.
   *
   * @param exchange the current server exchange
   * @param chain the filter chain
   * @return a Mono that signals completion
   */
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String corr =
        Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(HEADER))
            .orElse(UUID.randomUUID().toString());
    return chain.filter(exchange)
        .contextWrite(ctx -> ctx.put("corrId", corr));
  }
}
