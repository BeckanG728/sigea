package com.institucion.sigea.aula.service;

import com.institucion.sigea.aula.entity.AnioAcademico;
import java.util.List;

public interface AnioAcademicoService {
    List<AnioAcademico> listar();
    AnioAcademico crear(Integer anio);
    void activar(Long id);
}
