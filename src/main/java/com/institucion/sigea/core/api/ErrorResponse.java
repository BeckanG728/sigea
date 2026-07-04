package com.institucion.sigea.core.api;

import java.time.Instant;
import java.util.Map;


public record ErrorResponse(
        String error,
        String message,
        Map<String, Object> metadata,
        Instant timestamp
) {

    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(error, message, Map.of(), Instant.now());
    }

    public static ErrorResponse of(String error, String message, Map<String, Object> metadata) {
        return new ErrorResponse(error, message, metadata, Instant.now());
    }
}

