package com.institucion.sigea.reporte.service.impl;

import com.institucion.sigea.matricula.entity.EstadoCuota;
import com.institucion.sigea.matricula.repository.CuotaRepository;
import com.institucion.sigea.matricula.repository.DeudaDetalleRow;
import com.institucion.sigea.reporte.dto.response.DeudaDetalleReporteResponse;
import com.institucion.sigea.reporte.service.DeudaDetalleReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeudaDetalleReporteServiceImpl implements DeudaDetalleReporteService {

    /** Mismo criterio de "deuda" que usa PagoServiceImpl. */
    private static final List<EstadoCuota> ESTADOS_DEUDA =
            List.of(EstadoCuota.PENDIENTE);

    /**
     * Regla provisional de vencimiento (la tabla cuota aún no persiste una
     * fecha de vencimiento): la cuota con orden N corresponde al mes N del
     * año escolar que inicia en MARZO y vence el día 15 del mes siguiente.
     * Ej.: orden 1 (marzo) vence el 15/04; orden 2 (abril) vence el 15/05.
     * Si el equipo define otra regla, ajustar solo calcularVencimiento().
     */
    private static final int MES_INICIO_ANIO_ESCOLAR = 3; // marzo
    private static final int DIA_VENCIMIENTO = 15;

    private final CuotaRepository cuotaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DeudaDetalleReporteResponse> reportar(Integer anio, List<EstadoCuota> estados) {
        List<EstadoCuota> filtroEstados =
                (estados == null || estados.isEmpty()) ? ESTADOS_DEUDA : estados;

        LocalDate hoy = LocalDate.now();

        return cuotaRepository.reporteDeudasDetalle(filtroEstados, anio).stream()
                .map(fila -> toResponse(fila, hoy))
                .toList();
    }

    private DeudaDetalleReporteResponse toResponse(DeudaDetalleRow fila, LocalDate hoy) {
        LocalDate vencimiento = calcularVencimiento(fila.ordenPago(), fila.anioAcademico());
        long diasAtraso = Math.max(ChronoUnit.DAYS.between(vencimiento, hoy), 0);

        String alumno = fila.apellidoPaterno() + " " + fila.apellidoMaterno()
                + ", " + fila.nombres();
        String documento = fila.tipoDocumento() + " " + fila.numeroDocumento();

        return new DeudaDetalleReporteResponse(
                fila.codCuota(),
                alumno,
                documento,
                fila.nombreConcepto(),
                fila.montoPagar(),
                vencimiento,
                diasAtraso,
                fila.estadoCuota());
    }

    private LocalDate calcularVencimiento(short ordenPago, int anio) {
        // 15 de marzo del año académico + N meses => la cuota N vence
        // el día 15 del mes siguiente al que corresponde.
        return LocalDate.of(anio, MES_INICIO_ANIO_ESCOLAR, DIA_VENCIMIENTO)
                .plusMonths(ordenPago);
    }
}
