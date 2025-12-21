package com.bankx.transactions.infrastructure.config;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Utility for propagating MDC context in reactive flows.
 * Extracts correlation ID from reactor context and sets it in Log4j ThreadContext.
 */
@Component
public class LogContext {

  /**
   * Wraps a Mono with MDC context propagation.
   *
   * @param mono the Mono to wrap
   * @param <T> the type of elements emitted by the Mono
   * @return a Mono with MDC context applied
   */
  public <T> Mono<T> withMdc(Mono<T> mono) {
    return mono.deferContextual(ctx -> {
      String corr = ctx.getOrDefault("corrId", "na").toString();
      ThreadContext.put("corrId", corr);
      return mono.doFinally(sig -> ThreadContext.remove("corrId"));
    });
  }
}
