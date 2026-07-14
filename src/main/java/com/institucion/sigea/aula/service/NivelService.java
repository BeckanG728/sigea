package com.institucion.sigea.aula.service;

import com.institucion.sigea.aula.dto.response.NivelResponse;

import java.util.List;

public interface NivelService {
    List<NivelResponse> listar();
    void eliminar(Long id);
}
