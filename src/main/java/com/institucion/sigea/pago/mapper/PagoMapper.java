package com.institucion.sigea.pago.mapper;

import com.institucion.sigea.matricula.entity.Cuota;
import com.institucion.sigea.matricula.repository.DeudaAlumnoProjection;
import com.institucion.sigea.pago.dto.response.CuotaDeudaResponse;
import com.institucion.sigea.pago.dto.response.DeudaAlumnoResponse;
import com.institucion.sigea.pago.dto.response.PagoDetalleResponse;
import com.institucion.sigea.pago.dto.response.PagoResponse;
import com.institucion.sigea.pago.entity.Pago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PagoMapper {

    @Mapping(target = "codCuota", source = "id")
    @Mapping(target = "nombreConcepto", ignore = true)
    @Mapping(target = "anioAcademico", ignore = true)
    CuotaDeudaResponse toDeudaResponse(Cuota cuota);

    List<CuotaDeudaResponse> toDeudaResponseList(List<Cuota> cuotas);

    @Mapping(target = "codPago", source = "id")
    PagoDetalleResponse toDetalleResponse(Pago pago);

    List<PagoDetalleResponse> toDetalleResponseList(List<Pago> pagos);

    // codCuota en Pago es Integer, en Pago viene Integer y el DTO pide Long:
    // MapStruct genera el ensanchamiento (Integer -> Long) automáticamente.
    @Mapping(target = "codPago", source = "pago.id")
    @Mapping(target = "codCuota", source = "cuota.id")
    @Mapping(target = "numeroRecibo", source = "cuota.numeroRecibo")
    @Mapping(target = "montoPagado", source = "pago.montoPagado")
    @Mapping(target = "medioPago", source = "pago.medioPago")
    @Mapping(target = "fechaPago", source = "pago.fechaPago")
    PagoResponse toResponse(Pago pago, Cuota cuota);

    // DeudaAlumnoProjection: nombres iguales, mapeo directo aunque el origen
    // sea una projection de Spring Data y no una entidad.
    DeudaAlumnoResponse toDeudaAlumnoResponse(DeudaAlumnoProjection projection);

    List<DeudaAlumnoResponse> toDeudaAlumnoResponseList(List<DeudaAlumnoProjection> projections);
}
