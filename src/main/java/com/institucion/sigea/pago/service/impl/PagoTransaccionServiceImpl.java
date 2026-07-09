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
import com.institucion.sigea.pago.mapper.PagoMapper;
import com.institucion.sigea.pago.repository.PagoRepository;
import com.institucion.sigea.pago.repository.ReciboRepository;
import com.institucion.sigea.pago.service.PagoService;
import com.institucion.sigea.pago.service.PagoTransaccionService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PagoTransaccionServiceImpl implements PagoTransaccionService {

    private static final String FORMATO_NUMERO_RECIBO = "R-%d-%05d";

    private final PagoService pagoService;
    private final PagoRepository pagoRepository;
    private final ReciboRepository reciboRepository;
    private final CuotaRepository cuotaRepository;

    private final EntityManager entityManager;
    private final PagoMapper pagoMapper;

    @Override
    @Transactional
    @Auditable(modulo = "pago", operacion = TipoOperacionAuditoria.PAGO)
    public PagoResponse registrarPago(RegistrarPagoRequest request) {

        Cuota cuota = pagoService.validarOrdenDePago(request.codCuota());

        if (cuota.getMontoPagar().compareTo(request.montoPagado()) != 0) {
            throw new BusinessException(ErrorCode.MONTO_PAGO_INVALIDO,
                    "El monto pagado no coincide con el monto de la cuota",
                    Map.of("montoCuota", cuota.getMontoPagar(), "montoPagado", request.montoPagado()));
        }

        Pago pago = new Pago();
        pago.setCodCuota(cuota.getId().intValue());
        pago.setMontoPagado(request.montoPagado());
        pago.setMedioPago(request.medioPago());
        pago.setFechaPago(LocalDateTime.now());
        pagoRepository.save(pago);

        int anioActual = Year.now().getValue();
        Long siguienteCorrelativo = ((Number) entityManager
                .createNativeQuery("SELECT nextval('seq_numero_recibo')")
                .getSingleResult()).longValue();

        String numeroRecibo = FORMATO_NUMERO_RECIBO.formatted(anioActual, siguienteCorrelativo);

        Recibo recibo = new Recibo();
        recibo.setNumeroRecibo(numeroRecibo);
        recibo.setCodPago(pago.getId().intValue());
        recibo.setAnio(anioActual);
        recibo.setCorrelativo(siguienteCorrelativo.intValue());
        recibo.setFechaEmision(LocalDateTime.now());
        reciboRepository.save(recibo);

        cuota.setEstadoCuota(EstadoCuota.PAGADA);
        cuota.setNumeroRecibo(numeroRecibo);
        cuota.setFechaPago(LocalDateTime.now());
        cuotaRepository.save(cuota);

        return pagoMapper.toResponse(pago, cuota);
    }
}
