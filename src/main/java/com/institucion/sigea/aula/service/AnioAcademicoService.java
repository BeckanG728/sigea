package com.institucion.sigea.aula.service;

import com.institucion.sigea.aula.dto.response.AnioAcademicoResponse;
import java.util.List;

public interface AnioAcademicoService {
    List<AnioAcademicoResponse> listar();
    AnioAcademicoResponse crear(Integer anio);
    void activar(Long id);
    AnioAcademicoResponse obtenerActivo();
}
