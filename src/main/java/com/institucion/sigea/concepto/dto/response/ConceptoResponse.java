package com.institucion.sigea.concepto.dto.response;

import java.math.BigDecimal;

public record ConceptoResponse(
        Long id,
        String nombreConcepto,
        BigDecimal monto,
        short ordenPago,
        boolean obligatorio,
        Long version
) {}