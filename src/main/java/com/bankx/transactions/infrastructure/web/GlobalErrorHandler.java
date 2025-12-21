package com.bankx.transactions.infrastructure.web;

import com.bankx.transactions.exception.BusinessException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for REST controllers.
 * Provides centralized error handling and response formatting.
 */
@Slf4j
@RestControllerAdvice
public class GlobalErrorHandler {

  /**
   * Handles business exceptions.
   *
   * @param ex the business exception
   * @return a Mono emitting HTTP 400 response with error details
   */
  @ExceptionHandler(BusinessException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleBusinessException(
      BusinessException ex) {

    log.warn("Business error: {}", ex.getMessage());

    Map<String, Object> body = new HashMap<>();
    body.put("error", ex.getMessage());

    return Mono.just(ResponseEntity.badRequest().body(body));
  }

  /**
   * Handles validation exceptions.
   *
   * @param ex the validation exception
   * @return a Mono emitting HTTP 400 response with field validation errors
   */
  @ExceptionHandler(WebExchangeBindException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleValidationException(
      WebExchangeBindException ex) {

    log.warn("Validation error: {}", ex.getMessage());

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    Map<String, Object> body = new HashMap<>();
    body.put("errors", errors);

    return Mono.just(ResponseEntity.badRequest().body(body));
  }

  /**
   * Handles unexpected exceptions.
   *
   * @param ex the exception
   * @return a Mono emitting HTTP 500 response with error details
   */
  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(
      Exception ex) {

    log.error("Unexpected error", ex);

    Map<String, Object> body = new HashMap<>();
    body.put("error", "internal_error");
    body.put("details", ex.getMessage());

    return Mono.just(
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    );
  }
}
