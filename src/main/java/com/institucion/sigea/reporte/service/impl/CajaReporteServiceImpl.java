package com.institucion.sigea.reporte.service.impl;

import com.institucion.sigea.pago.repository.CajaRow;
import com.institucion.sigea.pago.repository.PagoRepository;
import com.institucion.sigea.reporte.dto.response.CajaFilaResponse;
import com.institucion.sigea.reporte.dto.response.CajaReporteResponse;
import com.institucion.sigea.reporte.service.CajaReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CajaReporteServiceImpl implements CajaReporteService {

    private static final Locale ES_PE = Locale.of("es", "PE");

    private final PagoRepository pagoRepository;

    @Override
    @Transactional(readOnly = true)
    public CajaReporteResponse reportar(LocalDateTime desde, LocalDateTime hasta) {
        List<CajaRow> pagos = pagoRepository.reporteCaja(desde, hasta);

        // Agrupar por (mes, tipo de concepto). LinkedHashMap conserva el orden
        // cronológico porque la query ya viene ordenada por fecha de pago.
        Map<Clave, Acumulado> grupos = new LinkedHashMap<>();
        for (CajaRow pago : pagos) {
            Clave clave = new Clave(YearMonth.from(pago.fechaPago()), pago.tipoConcepto());
            grupos.computeIfAbsent(clave, k -> new Acumulado()).sumar(pago.montoPagado());
        }

        List<CajaFilaResponse> filas = grupos.entrySet().stream()
                .map(e -> new CajaFilaResponse(
                        e.getKey().mes().getYear(),
                        e.getKey().mes().getMonthValue(),
                        nombreMes(e.getKey().mes()),
                        e.getKey().concepto(),
                        e.getValue().cantidad,
                        e.getValue().total))
                .toList();

        BigDecimal totalIngresos = filas.stream()
                .map(CajaFilaResponse::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // El módulo de egresos aún no existe: se reporta 0 (ver alerta en la vista).
        BigDecimal totalEgresos = BigDecimal.ZERO;

        return new CajaReporteResponse(
                totalIngresos,
                totalEgresos,
                totalIngresos.subtract(totalEgresos),
                filas);
    }

    private String nombreMes(YearMonth mes) {
        String nombre = mes.getMonth().getDisplayName(TextStyle.FULL, ES_PE);
        return nombre.substring(0, 1).toUpperCase(ES_PE) + nombre.substring(1);
    }

    private record Clave(YearMonth mes, String concepto) {}

    private static final class Acumulado {
        private long cantidad = 0;
        private BigDecimal total = BigDecimal.ZERO;

        private void sumar(BigDecimal monto) {
            cantidad++;
            total = total.add(monto);
        }
    }
}
