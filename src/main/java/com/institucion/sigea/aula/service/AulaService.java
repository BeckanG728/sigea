package com.institucion.sigea.aula.service;

import com.institucion.sigea.aula.dto.request.AulaRequest;
import com.institucion.sigea.aula.dto.response.AlumnoAulaResponse;
import com.institucion.sigea.aula.dto.response.AulaBusquedaResponse;
import com.institucion.sigea.aula.dto.response.AulaListadoResponse;
import com.institucion.sigea.aula.dto.response.AulaMatriculaResponse;
import com.institucion.sigea.aula.dto.response.AulaResponse;

import java.util.List;

public interface AulaService {
    AulaResponse crear(AulaRequest request);
    List<AulaBusquedaResponse> buscar(Long anioAcademicoId, Long nivelId);
    List<AulaListadoResponse> buscarAulas(Long anioAcademicoId, Long nivelId, Long gradoId, Boolean estado);
    List<AulaMatriculaResponse> buscarParaMatricula(Integer periodo);
    List<AulaListadoResponse> listarConDetalle(Long anioAcademicoId, Long nivelId);
    AulaListadoResponse obtenerPorId(Long id);
    List<AlumnoAulaResponse> obtenerAlumnos(Long aulaId, Long anioAcademicoId);
    AulaResponse actualizar(Long id, AulaRequest request);
    void eliminar(Long id);
}