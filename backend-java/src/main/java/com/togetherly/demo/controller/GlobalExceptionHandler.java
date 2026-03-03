package com.togetherly.demo.controller;

import com.togetherly.demo.data.ErrorMessageResponse;
import com.togetherly.demo.data.InvalidRequestResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler — catches exceptions thrown by any controller.
 *
 * @ControllerAdvice makes this apply to ALL controllers in the application.
 * Extends ResponseEntityExceptionHandler to get default handling for Spring MVC exceptions.
 *
 * THREE CASES:
 *
 * 1. handleMethodArgumentNotValid — Validation errors (@Valid failures)
 *    Returns 400 with field-level error messages:
 *    { "errors": { "username": ["cannot be empty !"], "email": ["invalid format !"] } }
 *
 * 2. handleHttpMessageNotReadable — Malformed JSON in request body
 *    Delegates to Spring's default handler (returns 400).
 *
 * 3. handleRuntimeException — Catch-all for unexpected errors
 *    Returns 500 with a generic message (hides internal details from clients).
 *
 * HAND-WRITTEN.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    @Override
    @ApiResponse(
            responseCode = "400",
            description = "field errors in request body/param",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = InvalidRequestResponse.class),
                    examples = @ExampleObject(
                            value = "{\"errors\":{\"field1\":[\"msg1\",\"msg2\"], "
                                    + "\"field2\":[...], ...}}")))
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Map<String, TreeSet<String>> errors = new HashMap<>();
        for (FieldError error : ex.getFieldErrors()) {
            TreeSet<String> messages = errors.getOrDefault(error.getField(), new TreeSet<>());
            messages.add(error.getDefaultMessage());
            errors.put(error.getField(), messages);
        }
        return ResponseEntity.badRequest().body(new InvalidRequestResponse(errors));
    }

    @ExceptionHandler(RuntimeException.class)
    @ApiResponse(
            responseCode = "500",
            description = "internal server error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class)))
    public ResponseEntity<?> handleRuntimeException(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        return new ResponseEntity<>(
                new ErrorMessageResponse("unknown error, please try again later !"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
