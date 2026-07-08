package com.institucion.sigea.alumno.mapper;

import com.institucion.sigea.alumno.dto.response.AlumnoBusquedaResponse;
import com.institucion.sigea.alumno.dto.response.AlumnoResponse;
import com.institucion.sigea.alumno.entity.Alumno;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;

@Mapper(componentModel = "spring", imports = LocalDate.class)
public interface AlumnoMapper {

    @Mapping(target = "fechaNacimiento", expression = "java(LocalDate.parse(alumno.getFechaNacimiento()))")
    AlumnoResponse toResponse(Alumno alumno);

    @Mapping(target = "nombreCompleto", expression =
            "java(alumno.getNombres() + \" \" + alumno.getApellidoPaterno() + \" \" + alumno.getApellidoMaterno())")
    AlumnoBusquedaResponse toBusquedaResponse(Alumno alumno);
}