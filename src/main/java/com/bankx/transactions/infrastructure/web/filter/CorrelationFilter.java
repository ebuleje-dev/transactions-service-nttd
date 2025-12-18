package com.bankx.transactions.infrastructure.web.filter;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class CorrelationFilter implements WebFilter {
    private static final String HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
        String corr = Optional.ofNullable(
            exchange.getRequest().getHeaders().getFirst(HEADER))
            .orElse(UUID.randomUUID().toString());

        return chain.filter(exchange)
            .contextWrite(ctx -> ctx.put("corrId", corr));
    }
}
