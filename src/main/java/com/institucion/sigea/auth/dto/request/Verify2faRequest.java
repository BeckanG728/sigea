package com.institucion.sigea.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record Verify2faRequest(
        @NotNull(message = "El usuario es obligatorio")
        Long idUsuario,

        @NotBlank(message = "El código de verificación es obligatorio")
        String codigoTotp
) {
}
