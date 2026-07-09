package com.institucion.sigea.aula.service;

import com.institucion.sigea.aula.entity.Grado;

import java.util.List;

public interface GradoService {
    List<Grado> listar();
    void eliminar(Long id);
}
