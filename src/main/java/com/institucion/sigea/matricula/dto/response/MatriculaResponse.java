package com.institucion.sigea.matricula.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record MatriculaResponse(
        Long id,
        String codigo,
        Integer codAlumno,
        Integer codAula,
        Integer codAnioAcademico,
        LocalDateTime fechaMatricula,
        List<CuotaResponse> cuotas,
        Boolean requiresQrSetup,
        String qrUri
) {

    public static MatriculaResponse withQrSetup(MatriculaResponse original, String qrUri) {
        return new MatriculaResponse(
                original.id(), original.codigo(), original.codAlumno(), original.codAula(),
                original.codAnioAcademico(), original.fechaMatricula(), original.cuotas(),
                true, qrUri
        );
    }

}
