package com.institucion.sigea.alumno.service;

import com.institucion.sigea.alumno.dto.request.AlumnoRequest;
import com.institucion.sigea.alumno.dto.response.AlumnoBusquedaResponse;
import com.institucion.sigea.alumno.dto.response.AlumnoMatriculaResponse;
import com.institucion.sigea.alumno.dto.response.AlumnoResponse;
import com.institucion.sigea.pago.dto.response.DeudaMatriculaResponse;

import java.util.List;

public interface AlumnoService {
    AlumnoResponse crear(AlumnoRequest request);
    List<AlumnoBusquedaResponse> buscar(String nombres);
    List<AlumnoMatriculaResponse> buscarParaMatricula(String q);
    List<DeudaMatriculaResponse> listarDeudasMatricula(Long alumnoId, Integer anio);
    List<AlumnoBusquedaResponse> buscarPorDocumento(String numero);
    AlumnoResponse buscarPorDni(String dni);
    void eliminar(Long id);
}