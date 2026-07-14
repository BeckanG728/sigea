package com.institucion.sigea.alumno.service;

import com.institucion.sigea.alumno.dto.request.AlumnoRequest;
import com.institucion.sigea.alumno.dto.response.AlumnoBusquedaResponse;
import com.institucion.sigea.alumno.dto.response.AlumnoResponse;

import java.util.List;

public interface AlumnoService {
    AlumnoResponse crear(AlumnoRequest request);
    List<AlumnoBusquedaResponse> buscar(String nombres);
    List<AlumnoBusquedaResponse> buscarPorDocumento(String numero);
    void eliminar(Long id);
}