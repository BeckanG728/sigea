package com.institucion.sigea.matricula.dto.response;

import com.institucion.sigea.alumno.dto.response.AlumnoMatriculaResponse;
import com.institucion.sigea.aula.dto.response.AulaMatriculaResponse;
import com.institucion.sigea.aula.dto.response.AnioAcademicoResponse;
import com.institucion.sigea.concepto.dto.response.ConceptoResponse;

import java.math.BigDecimal;
import java.util.List;

public record MatriculaPreviewResponse(
        boolean valido,
        List<String> errores,
        AlumnoMatriculaResponse alumno,
        AulaMatriculaResponse aula,
        AnioAcademicoResponse anio,
        List<ConceptoResponse> conceptos,
        BigDecimal total,
        Cupos cupos,
        boolean totpVerificado
) {
    public record Cupos(long capacidad, long matriculados, long vacantes) {}

    public static MatriculaPreviewResponse invalido(List<String> errores) {
        return new MatriculaPreviewResponse(false, errores, null, null, null, List.of(), BigDecimal.ZERO, new Cupos(0, 0, 0), false);
    }

    public static MatriculaPreviewResponse valido(
            AlumnoMatriculaResponse alumno,
            AulaMatriculaResponse aula,
            AnioAcademicoResponse anio,
            List<ConceptoResponse> conceptos,
            BigDecimal total,
            long capacidad,
            long matriculados,
            long vacantes,
            boolean totpVerificado) {
        return new MatriculaPreviewResponse(true, List.of(), alumno, aula, anio, conceptos, total, new Cupos(capacidad, matriculados, vacantes), totpVerificado);
    }
}