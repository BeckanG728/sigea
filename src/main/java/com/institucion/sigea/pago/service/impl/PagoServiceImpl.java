package com.institucion.sigea.pago.service.impl;

import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.matricula.entity.Cuota;
import com.institucion.sigea.matricula.entity.EstadoCuota;
import com.institucion.sigea.matricula.repository.CuotaRepository;
import com.institucion.sigea.pago.dto.response.CuotaDeudaResponse;
import com.institucion.sigea.pago.dto.response.DeudaAlumnoResponse;
import com.institucion.sigea.pago.dto.response.PagoDetalleResponse;
import com.institucion.sigea.pago.dto.response.PagoReporteResponse;
import com.institucion.sigea.pago.entity.Pago;
import com.institucion.sigea.pago.repository.PagoRepository;
import com.institucion.sigea.pago.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private static final List<EstadoCuota> ESTADOS_DEUDA = List.of(EstadoCuota.PENDIENTE, EstadoCuota.BLOQUEADA);

    private final CuotaRepository cuotaRepository;
    private final PagoRepository pagoRepository;

    @Override
    public List<CuotaDeudaResponse> listarDeudas(Long codAlumno) {
        return cuotaRepository.findDeudasPorAlumno(codAlumno.intValue(), ESTADOS_DEUDA).stream()
                .map(c -> new CuotaDeudaResponse(
                        c.getId(), c.getCodMatricula(), c.getMontoPagar(), c.getOrdenPago(), c.getEstadoCuota()))
                .toList();
    }

    @Override
    public Cuota validarOrdenDePago(Long codCuota) {
        Cuota cuota = cuotaRepository.findById(codCuota)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CUOTA_NO_ENCONTRADA, "Cuota no encontrada",
                        Map.of("codCuota", codCuota)));

        if (cuota.getEstadoCuota() == EstadoCuota.PAGADA) {
            throw new BusinessException(ErrorCode.CUOTA_YA_PAGADA, "La cuota ya fue pagada",
                    Map.of("codCuota", codCuota));
        }

        List<Cuota> anterioresPendientes = cuotaRepository
                .findByCodMatriculaAndEstadoCuotaInAndOrdenPagoLessThanOrderByOrdenPagoAsc(
                        cuota.getCodMatricula(), ESTADOS_DEUDA, cuota.getOrdenPago());

        if (!anterioresPendientes.isEmpty()) {
            Cuota anterior = anterioresPendientes.get(0);
            throw new BusinessException(ErrorCode.CUOTA_ANTERIOR_PENDIENTE,
                    "Existe una cuota anterior pendiente de pago",
                    Map.of("codCuota", codCuota, "codCuotaPendienteAnterior", anterior.getId()));
        }

        return cuota;
    }
    @Override
    public PagoReporteResponse reportarPagos(LocalDateTime desde, LocalDateTime hasta) {
        List<Pago> pagos = pagoRepository.findByFechaPagoBetween(desde, hasta);

        BigDecimal total = pagos.stream()
                .map(Pago::getMontoPagado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PagoDetalleResponse> detalle = pagos.stream()
                .map(p -> new PagoDetalleResponse(
                        p.getId(), p.getCodCuota().longValue(), p.getMontoPagado(), p.getMedioPago(), p.getFechaPago()))
                .toList();

        return new PagoReporteResponse(total, pagos.size(), detalle);
    }

    @Override
    public List<DeudaAlumnoResponse> reportarDeudasConsolidadas() {
        return cuotaRepository.reporteDeudasPorAlumno(ESTADOS_DEUDA).stream()
                .map(p -> new DeudaAlumnoResponse(p.getCodAlumno(), p.getMontoAdeudado(), p.getCantidadCuotas()))
                .toList();
    }
}
