package com.bankx.transactions.infrastructure.config;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LogContext {

    public <T> Mono<T> withMdc(final Mono<T> mono) {
        return mono.deferContextual(ctx -> {
            String corr = ctx.getOrDefault("corrId", "na").toString();
            ThreadContext.put("corrId", corr);
            return mono.doFinally(sig -> ThreadContext.remove("corrId"));
        });
    }
}
