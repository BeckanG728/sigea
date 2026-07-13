package com.institucion.sigea.aula.mapper;

import com.institucion.sigea.aula.dto.response.AulaBusquedaResponse;
import com.institucion.sigea.aula.dto.response.AulaResponse;
import com.institucion.sigea.aula.entity.Aula;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.institucion.sigea.aula.dto.response.AulaListadoResponse;

@Mapper(componentModel = "spring")
public interface AulaMapper {

    AulaResponse toResponse(Aula aula);

    @Mapping(target = "vacantesDisponibles", source = "capacidadMaxima")
    @Mapping(target = "descripcion", expression =
            "java(aula.getAnioAcademico().getAnio() + \" - \" + aula.getNivel().getNombre() + \" - \" " +
                    "+ aula.getGrado().getNombreGrado() + \" \" + aula.getSeccion())")
    AulaBusquedaResponse toBusquedaResponse(Aula aula);

    @Mapping(target = "nivel", source = "nivel.nombre")
    @Mapping(target = "grado", source = "grado.nombreGrado")
    @Mapping(target = "matriculados", ignore = true)
    @Mapping(target = "vacantes", ignore = true)
    AulaListadoResponse toListadoResponse(Aula aula);
}