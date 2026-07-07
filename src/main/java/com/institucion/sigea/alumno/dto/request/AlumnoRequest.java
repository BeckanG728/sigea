package com.institucion.sigea.alumno.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record AlumnoRequest(
        @NotNull Long codTipoDocumento,
        @NotBlank @Pattern(regexp = "\\d{8,15}") String numeroDocumento,
        @NotBlank @Pattern(regexp = "[\\p{L} ]+") String nombres,
        @NotBlank @Pattern(regexp = "[\\p{L} ]+") String apellidoPaterno,
        @NotBlank @Pattern(regexp = "[\\p{L} ]+") String apellidoMaterno,
        @NotNull @Past LocalDate fechaNacimiento
) {}