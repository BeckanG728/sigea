package com.institucion.sigea.pago.dto.response;

import java.math.BigDecimal;

public record DeudaHistorialResponse(
        Long codCuota,
        Integer codAlumno,
        String tipoDocumento,
        String numeroDocumento,
        String alumno,
        String concepto,
        Integer anioAcademico,
        BigDecimal monto,
        String estado
) {}
