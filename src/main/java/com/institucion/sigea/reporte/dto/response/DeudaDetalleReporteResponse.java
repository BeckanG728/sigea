package com.institucion.sigea.reporte.dto.response;

import com.institucion.sigea.matricula.entity.EstadoCuota;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Fila del "Reporte de Deudas" (una por cuota adeudada), lista para
 * mostrarse en la tabla del frontend.
 */
public record DeudaDetalleReporteResponse(
        Long codCuota,
        String alumno,           // "Apellidos, Nombres"
        String documento,        // "DNI 75412638"
        String concepto,         // nombre del concepto, ej. "Pensión Marzo"
        BigDecimal monto,
        LocalDate fechaVencimiento,
        long diasAtraso,
        EstadoCuota estado       // PENDIENTE | BLOQUEADA
) {}
