package com.institucion.sigea.matricula.mapper;

import com.institucion.sigea.matricula.dto.response.CuotaResponse;
import com.institucion.sigea.matricula.dto.response.MatriculaReporteResponse;
import com.institucion.sigea.matricula.dto.response.MatriculaResponse;
import com.institucion.sigea.matricula.entity.Cuota;
import com.institucion.sigea.matricula.entity.Matricula;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MatriculaMapper {

    CuotaResponse toResponse(Cuota cuota);

    List<CuotaResponse> toResponseList(List<Cuota> cuotas);

    MatriculaReporteResponse toReporteResponse(Matricula matricula);

    List<MatriculaReporteResponse> toReporteResponseList(List<Matricula> matriculas);

    @Mapping(target = "cuotas", source = "cuotas")
    MatriculaResponse toResponse(Matricula matricula, List<Cuota> cuotas);
}
