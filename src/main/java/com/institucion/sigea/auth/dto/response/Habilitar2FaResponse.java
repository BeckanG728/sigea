package com.institucion.sigea.auth.dto.response;

public record Habilitar2FaResponse(
        String secretoQr,
        boolean login2fa
) {
}
