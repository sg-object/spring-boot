package com.sg.spring.validation.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TestControllerAdvice {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handle(final MethodArgumentNotValidException e) {
    final var sb = new StringBuilder();
    sb.append("\n");
    sb.append("======================\n");
    sb.append("======================\n");
    e.getFieldErrors().forEach(ee -> {
      sb.append(ee.getField());
      sb.append(": ");
      sb.append(ee.getDefaultMessage());
      sb.append("\n");
    });
    sb.append("======================\n");
    sb.append("======================");
    return ResponseEntity.badRequest().body(sb.toString());
  }
}
