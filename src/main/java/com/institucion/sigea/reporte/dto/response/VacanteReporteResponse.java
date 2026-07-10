package com.institucion.sigea.reporte.dto.response;

public record VacanteReporteResponse(
        Long codAula,
        String descripcion,
        Integer anioAcademico,
        String nivel,
        String grado,
        String seccion,
        short capacidadMaxima,
        long matriculados,
        long vacantesDisponibles
) {}
