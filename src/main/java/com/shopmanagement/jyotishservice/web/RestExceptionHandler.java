package com.shopmanagement.jyotishservice.web;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import com.shopmanagement.jyotishservice.entitlement.JyotishEntitlementException;

@RestControllerAdvice
public class RestExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

  @ExceptionHandler(JyotishEntitlementException.class)
  public ResponseEntity<Map<String, Object>> handleEntitlement(JyotishEntitlementException ex) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("message", ex.getMessage());
    payload.put("code", "JYOTISH_ENTITLEMENT_DENIED");
    payload.put("status", HttpStatus.FORBIDDEN.value());
    payload.put("timestamp", Instant.now().toString());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(payload);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
    return body(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Map<String, Object>> handleMissingParam(
      MissingServletRequestParameterException ex) {
    return body(HttpStatus.BAD_REQUEST, "Missing required parameter: " + ex.getParameterName());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map<String, Object>> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    String name = ex.getName() != null ? ex.getName() : "parameter";
    return body(HttpStatus.BAD_REQUEST, "Invalid value for parameter: " + name);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .orElse("Validation failed");
    return body(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
    log.warn("Unreadable request body: {}", rootMessage(ex));
    return body(
        HttpStatus.BAD_REQUEST,
        "Invalid request body. Check date (yyyy-MM-dd), time (HH:mm), and JSON field types.");
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
    return body(status, message);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
    log.error("Unhandled error on Jyotish API request", ex);
    return body(HttpStatus.INTERNAL_SERVER_ERROR, "We couldn't complete that request. Please try again.");
  }

  private static String rootMessage(Throwable ex) {
    Throwable cur = ex;
    while (cur.getCause() != null && cur.getCause() != cur) {
      cur = cur.getCause();
    }
    String msg = cur.getMessage();
    return msg != null ? msg : ex.getClass().getSimpleName();
  }

  private static ResponseEntity<Map<String, Object>> body(HttpStatus status, String message) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("message", message);
    payload.put("status", status.value());
    payload.put("timestamp", Instant.now().toString());
    return ResponseEntity.status(status).body(payload);
  }
}
