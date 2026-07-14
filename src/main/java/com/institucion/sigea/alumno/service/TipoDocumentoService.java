package com.institucion.sigea.alumno.service;

import com.institucion.sigea.alumno.dto.response.TipoDocumentoResponse;

import java.util.List;

public interface TipoDocumentoService {
    List<TipoDocumentoResponse> listar();
    void eliminar(Long id);
}