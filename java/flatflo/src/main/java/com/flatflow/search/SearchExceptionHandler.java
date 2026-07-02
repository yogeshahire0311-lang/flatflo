package com.flatflow.search;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Maps search input errors to consistent 400 responses
 * ({@code {"error": code, "message": ...}}) per the API contract.
 */
@ControllerAdvice
public class SearchExceptionHandler {

    @ExceptionHandler(InvalidSearchException.class)
    public ResponseEntity<Map<String, String>> handleInvalid(InvalidSearchException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.error(), "message", ex.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissing(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "MISSING_PARAMETER",
                        "message", "Missing required parameter '" + ex.getParameterName() + "'."));
    }
}
