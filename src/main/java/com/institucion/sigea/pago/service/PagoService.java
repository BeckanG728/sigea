package com.institucion.sigea.pago.service;

import com.institucion.sigea.matricula.entity.Cuota;
import com.institucion.sigea.pago.dto.response.CuotaDeudaResponse;

import java.util.List;

public interface PagoService {

    List<CuotaDeudaResponse> listarDeudas(Long codAlumno);

    Cuota validarOrdenDePago(Long codCuota);
}
