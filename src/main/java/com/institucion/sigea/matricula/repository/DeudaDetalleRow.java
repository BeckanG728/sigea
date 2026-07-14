package com.institucion.sigea.matricula.repository;

import com.institucion.sigea.matricula.entity.EstadoCuota;

import java.math.BigDecimal;

/**
 * Fila cruda del reporte detallado de deudas (una fila por cuota adeudada).
 * Se llena vía expresión constructora en {CuotaRepository#reporteDeudasDetalle}.
 */
public record DeudaDetalleRow(
        Long codCuota,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String tipoDocumento,
        String numeroDocumento,
        String nombreConcepto,
        Integer anioAcademico,
        Short ordenPago,
        BigDecimal montoPagar,
        EstadoCuota estadoCuota
) {}
