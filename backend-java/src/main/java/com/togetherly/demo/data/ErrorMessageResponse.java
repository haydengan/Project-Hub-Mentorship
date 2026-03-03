package com.togetherly.demo.data;

/**
 * Returned to the client when an error occurs.
 * Example JSON: { "message": "username already exists" }
 *
 * JAVA RECORD — immutable DTO. The compiler auto-generates:
 *   - Constructor: new ErrorMessageResponse("some error")
 *   - Getter:      response.message()  (NOT getMessage())
 *   - equals(), hashCode(), toString()
 *
 * Records replace Lombok @Data for DTOs. They're built into Java 17+,
 * so no library dependency needed.
 */
public record ErrorMessageResponse(String message) {}
