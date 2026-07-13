package com.institucion.sigea.usuario.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RolRequest(
        @NotBlank
        @Pattern(regexp = "\\S+", message = "El nombre no puede contener espacios")
        String nombre
) {

}
