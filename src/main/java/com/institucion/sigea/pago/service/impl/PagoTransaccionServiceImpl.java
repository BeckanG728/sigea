package com.institucion.sigea.pago.service.impl;

import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.matricula.entity.Cuota;
import com.institucion.sigea.matricula.entity.EstadoCuota;
import com.institucion.sigea.matricula.repository.CuotaRepository;
import com.institucion.sigea.pago.dto.request.RegistrarPagoRequest;
import com.institucion.sigea.pago.dto.response.PagoResponse;
import com.institucion.sigea.pago.entity.Pago;
import com.institucion.sigea.pago.entity.Recibo;
import com.institucion.sigea.pago.repository.PagoRepository;
import com.institucion.sigea.pago.repository.ReciboRepository;
import com.institucion.sigea.pago.service.PagoService;
import com.institucion.sigea.pago.service.PagoTransaccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Map;

/**
 * Secuencia exacta exigida por el enunciado:
 * Generar Recibo → Actualizar Correlativo → Marcar Cuota como Pagada →
 * Registrar Auditoría → Commit, con rollback total ante error.
 *
 * @Auditable + AuditoriaAspect (P1-11) escriben la fila de auditoría
 * dentro de esta misma transacción, antes del commit real.
 */
@Service
@RequiredArgsConstructor
public class PagoTransaccionServiceImpl implements PagoTransaccionService {

    private static final String FORMATO_NUMERO_RECIBO = "R-%d-%05d";

    private final PagoService pagoService;
    private final PagoRepository pagoRepository;
    private final ReciboRepository reciboRepository;
    private final CuotaRepository cuotaRepository;

    @Override
    @Transactional
    @Auditable(modulo = "pago", operacion = TipoOperacionAuditoria.PAGO)
    public PagoResponse registrarPago(RegistrarPagoRequest request) {

        // 0. Validar orden de pago (P3-05): existe, no está ya pagada, y no
        // hay cuotas anteriores de la misma matrícula sin pagar.
        Cuota cuota = pagoService.validarOrdenDePago(request.codCuota());

        if (cuota.getMontoPagar().compareTo(request.montoPagado()) != 0) {
            throw new BusinessException(ErrorCode.MONTO_PAGO_INVALIDO,
                    "El monto pagado no coincide con el monto de la cuota",
                    Map.of("montoCuota", cuota.getMontoPagar(), "montoPagado", request.montoPagado()));
        }

        // 1. Generar Pago
        Pago pago = new Pago();
        pago.setCodCuota(cuota.getId().intValue());
        pago.setMontoPagado(request.montoPagado());
        pago.setMedioPago(request.medioPago());
        pago.setFechaPago(LocalDateTime.now());
        pagoRepository.save(pago);

        // 2. Generar Recibo + Actualizar Correlativo (bloqueado con
        // PESSIMISTIC_WRITE en ReciboRepository para serializar concurrencia
        // dentro del mismo año).
        int anioActual = Year.now().getValue();
        int siguienteCorrelativo = reciboRepository.findFirstByAnioOrderByCorrelativoDesc(anioActual)
                .map(ultimo -> ultimo.getCorrelativo() + 1)
                .orElse(1);

        String numeroRecibo = FORMATO_NUMERO_RECIBO.formatted(anioActual, siguienteCorrelativo);

        Recibo recibo = new Recibo();
        recibo.setNumeroRecibo(numeroRecibo);
        recibo.setCodPago(pago.getId().intValue());
        recibo.setAnio(anioActual);
        recibo.setCorrelativo(siguienteCorrelativo);
        recibo.setFechaEmision(LocalDateTime.now());
        reciboRepository.save(recibo);

        // 3. Marcar Cuota como Pagada
        cuota.setEstadoCuota(EstadoCuota.PAGADA);
        cuota.setNumeroRecibo(numeroRecibo);
        cuota.setFechaPago(LocalDateTime.now());
        cuotaRepository.save(cuota);

        // 4. Registrar Auditoría → Commit: delegado en @Auditable (arriba).

        return new PagoResponse(
                pago.getId(),
                cuota.getId(),
                numeroRecibo,
                pago.getMontoPagado(),
                pago.getMedioPago(),
                pago.getFechaPago());
    }
}
