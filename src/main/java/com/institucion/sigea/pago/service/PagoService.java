package com.institucion.sigea.pago.service;

import com.institucion.sigea.matricula.entity.Cuota;
import com.institucion.sigea.pago.dto.response.CuotaDeudaResponse;
import com.institucion.sigea.pago.dto.response.DeudaAlumnoResponse;
import com.institucion.sigea.pago.dto.response.HistorialGeneralResponse;
import com.institucion.sigea.pago.dto.response.PagoReporteResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoService {

    List<CuotaDeudaResponse> listarDeudas(Long codAlumno);

    HistorialGeneralResponse listarHistorialGeneral(Pageable pageable);

    Cuota validarOrdenDePago(Long codCuota);

    PagoReporteResponse reportarPagos(LocalDateTime desde, LocalDateTime hasta);
    List<DeudaAlumnoResponse> reportarDeudasConsolidadas();
}
