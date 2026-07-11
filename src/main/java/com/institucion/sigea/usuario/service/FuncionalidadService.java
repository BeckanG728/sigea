package com.institucion.sigea.usuario.service;

import com.institucion.sigea.usuario.dto.response.FuncionalidadTreeResponse;
import com.institucion.sigea.usuario.dto.response.MisPermisosResponse;

import java.util.List;

public interface FuncionalidadService {

    List<FuncionalidadTreeResponse> obtenerArbol();
    List<MisPermisosResponse> obtenerMisPermisos(Long idRol);
}
