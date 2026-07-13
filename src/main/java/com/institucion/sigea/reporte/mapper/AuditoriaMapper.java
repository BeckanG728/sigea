package com.institucion.sigea.reporte.mapper;

import com.institucion.sigea.auditoria.AuditoriaEntity;
import com.institucion.sigea.reporte.dto.response.AuditoriaReporteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuditoriaMapper {

    @Mapping(target = "codUsuario", source = "usuario.id")
    @Mapping(target = "nombreUsuario", source = "usuario.nombre")
    @Mapping(target = "tablaAfectada", source = "tablaAfectada")
    AuditoriaReporteResponse toResponse(AuditoriaEntity auditoria);

    List<AuditoriaReporteResponse> toResponseList(List<AuditoriaEntity> auditorias);
}