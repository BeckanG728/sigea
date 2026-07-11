package com.institucion.sigea.usuario.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RolRequest(@NotBlank String nombreRol) {

}
