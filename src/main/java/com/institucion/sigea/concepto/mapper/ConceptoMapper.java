package com.institucion.sigea.concepto.mapper;

import com.institucion.sigea.concepto.dto.response.ConceptoResponse;
import com.institucion.sigea.concepto.entity.Concepto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConceptoMapper {
    ConceptoResponse toResponse(Concepto concepto); // todos los campos coinciden de nombre
}