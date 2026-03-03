package com.togetherly.demo.data;

import java.util.Map;
import java.util.TreeSet;

/**
 * Returned when request validation fails. Maps field names to their error messages.
 * Example JSON: { "errors": { "username": ["too short", "contains spaces"] } }
 *
 * TreeSet keeps error messages sorted alphabetically for consistent output.
 */
public record InvalidRequestResponse(Map<String, TreeSet<String>> errors) {}
