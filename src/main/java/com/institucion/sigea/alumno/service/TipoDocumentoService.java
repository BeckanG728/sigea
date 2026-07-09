package com.institucion.sigea.alumno.service;

import com.institucion.sigea.alumno.entity.TipoDocumento;

import java.util.List;

public interface TipoDocumentoService {
    List<TipoDocumento> listar();
    void eliminar(Long id);
}