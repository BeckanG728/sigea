package com.institucion.sigea.usuario.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearUsuarioRequest(

        @NotNull(message = "El rol es obligatorio")
        Long idRol,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank
        @Size(max = 100)
        String primerApellido,

        @NotBlank
        @Pattern(regexp = "\\d{8,15}")
        String numeroDocumento

) {
}
