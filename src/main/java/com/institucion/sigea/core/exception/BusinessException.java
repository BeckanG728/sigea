package com.institucion.sigea.core.exception;

import lombok.Getter;

import java.util.Map;

/**
 * Excepción de negocio común. El {@link ErrorCode} determina tanto el
 * código HTTP de respuesta como el valor del campo {@code error} en
 * {@link com.institucion.sigea.core.api.ErrorResponse} — ver
 * {@link GlobalExceptionHandler}.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> metadata;

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, Map.of());
    }

    public BusinessException(ErrorCode errorCode, String message, Map<String, Object> metadata) {
        super(message);
        this.errorCode = errorCode;
        this.metadata = metadata == null ? Map.of() : metadata;
    }
}
