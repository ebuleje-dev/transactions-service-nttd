package com.bankx.transactions.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for business rule violations.
 * Maps to HTTP 400 Bad Request status.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

  /**
   * Constructs a new business exception with the specified message.
   *
   * @param message the error message
   */
  public BusinessException(String message) {
    super(message);
  }
}
