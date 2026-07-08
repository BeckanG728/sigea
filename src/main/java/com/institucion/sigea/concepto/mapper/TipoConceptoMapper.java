package com.institucion.sigea.concepto.mapper;

import com.institucion.sigea.concepto.dto.response.TipoConceptoResponse;
import com.institucion.sigea.concepto.entity.TipoConcepto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TipoConceptoMapper {
    TipoConceptoResponse toResponse(TipoConcepto tipoConcepto); // id, nombre, estado coinciden de nombre
}