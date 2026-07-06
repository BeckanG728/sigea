package com.institucion.sigea.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record Habilitar2FaRequest(
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
