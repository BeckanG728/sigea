package com.institucion.sigea.core.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    LOGIN_BLOCKED(HttpStatus.TOO_MANY_REQUESTS),
    INVALID_TOTP(HttpStatus.UNAUTHORIZED),
    TWOFA_SESSION_EXPIRED(HttpStatus.GONE),
    TWOFA_REQUIRED(HttpStatus.FORBIDDEN),
    USUARIO_NO_ELIMINABLE(HttpStatus.CONFLICT),
    PERMISO_DENEGADO(HttpStatus.FORBIDDEN),
    AULA_DUPLICADA(HttpStatus.CONFLICT),
    ALUMNO_DUPLICADO(HttpStatus.CONFLICT),
    CONCEPTO_DUPLICADO(HttpStatus.CONFLICT),
    VERSION_CONFLICT(HttpStatus.CONFLICT),
    AULA_SIN_VACANTES(HttpStatus.UNPROCESSABLE_ENTITY),
    CUOTA_ANTERIOR_PENDIENTE(HttpStatus.BAD_REQUEST),
    VALIDACION_FORMULARIO(HttpStatus.BAD_REQUEST),
    ROL_NO_ENCONTRADO(HttpStatus.NOT_FOUND),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    TIPO_CONCEPTO_DUPLICADO(HttpStatus.CONFLICT),
    CLONADO_ANIOS_IGUALES(HttpStatus.BAD_REQUEST),
    TIPO_CONCEPTO_NO_ENCONTRADO(HttpStatus.NOT_FOUND);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
