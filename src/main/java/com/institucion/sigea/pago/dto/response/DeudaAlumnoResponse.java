package com.institucion.sigea.pago.dto.response;

import java.math.BigDecimal;

public record DeudaAlumnoResponse(
        Integer codAlumno,
        BigDecimal montoAdeudado,
        Long cantidadCuotas
) {}
