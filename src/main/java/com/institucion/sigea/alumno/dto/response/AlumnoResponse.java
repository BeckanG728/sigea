package com.institucion.sigea.alumno.dto.response;

import java.time.LocalDate;

public record AlumnoResponse(
        Long id,
        String numeroDocumento,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        LocalDate fechaNacimiento
) {}