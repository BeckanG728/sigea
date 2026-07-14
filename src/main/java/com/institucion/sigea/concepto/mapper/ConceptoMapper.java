package com.institucion.sigea.concepto.mapper;

import com.institucion.sigea.concepto.dto.response.ConceptoResponse;
import com.institucion.sigea.concepto.entity.Concepto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConceptoMapper {

    @Mapping(target = "tipoConceptoId", source = "tipoConcepto.id")
    @Mapping(target = "tipoConceptoNombre", source = "tipoConcepto.nombre")
    @Mapping(target = "anioAcademicoId", source = "anioAcademico.id")
    @Mapping(target = "anioAcademico", source = "anioAcademico.anio")
    ConceptoResponse toResponse(Concepto concepto);
}