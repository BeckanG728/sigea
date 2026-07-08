package com.institucion.sigea.aula.mapper;

import com.institucion.sigea.aula.dto.response.AulaBusquedaResponse;
import com.institucion.sigea.aula.dto.response.AulaResponse;
import com.institucion.sigea.aula.entity.Aula;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AulaMapper {

    AulaResponse toResponse(Aula aula); // id, seccion y capacidadMaxima coinciden de nombre, MapStruct los mapea solo

    @Mapping(target = "vacantesDisponibles", source = "capacidadMaxima")
    @Mapping(target = "descripcion", expression =
            "java(aula.getAnioAcademico().getAnio() + \" - \" + aula.getNivel().getNombre() + \" - \" " +
                    "+ aula.getGrado().getNombreGrado() + \" \" + aula.getSeccion())")
    AulaBusquedaResponse toBusquedaResponse(Aula aula);
}