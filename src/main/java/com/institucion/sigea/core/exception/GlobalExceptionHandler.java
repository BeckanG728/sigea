package com.institucion.sigea.core.exception;

import com.institucion.sigea.core.api.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Traduce excepciones a la forma estándar {@link ErrorResponse}. El código
 * HTTP y el string de {@code error} salen siempre de {@link ErrorCode},
 * nunca de {@code HttpStatus.name()} — así el frontend puede hacer switch
 * sobre valores estables como {@code INVALID_CREDENTIALS} en vez de
 * genéricos como {@code CONFLICT}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse body = ErrorResponse.of(errorCode.name(), ex.getMessage(), ex.getMetadata());
        return ResponseEntity.status(errorCode.httpStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> metadata = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            metadata.put(fieldError.getField(),
                    fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "inválido");
        }
        ErrorResponse body = ErrorResponse.of(
                ErrorCode.VALIDACION_FORMULARIO.name(), "Uno o más campos son inválidos", metadata);
        return ResponseEntity.status(ErrorCode.VALIDACION_FORMULARIO.httpStatus()).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.VALIDACION_FORMULARIO.name(), ex.getMessage());
        return ResponseEntity.status(ErrorCode.VALIDACION_FORMULARIO.httpStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.INTERNAL_ERROR.name(), "Ocurrió un error inesperado");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
