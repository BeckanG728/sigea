package com.institucion.sigea.aula.service;

import com.institucion.sigea.aula.entity.Nivel;

import java.util.List;

public interface NivelService {
    List<Nivel> listar();
    void eliminar(Long id);
}
