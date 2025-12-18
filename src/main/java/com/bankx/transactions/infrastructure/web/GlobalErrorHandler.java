package com.bankx.transactions.infrastructure.web;

import com.bankx.transactions.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleBusinessException(
            BusinessException ex) {

        log.warn("Business error: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());

        return Mono.just(ResponseEntity.badRequest().body(body));
    }

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