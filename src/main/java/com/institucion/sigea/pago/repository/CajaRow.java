package com.institucion.sigea.pago.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Fila cruda para el reporte de caja: cada pago con el tipo de concepto
 * (Matrícula, Pensión, etc.) de la cuota que canceló.
 * Se llena vía expresión constructora en {PagoRepository#reporteCaja}.
 */
public record CajaRow(
        LocalDateTime fechaPago,
        String tipoConcepto,
        BigDecimal montoPagado
) {}
