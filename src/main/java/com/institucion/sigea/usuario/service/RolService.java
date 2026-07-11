package com.institucion.sigea.usuario.service;

import com.institucion.sigea.usuario.dto.request.RolRequest;
import com.institucion.sigea.usuario.dto.response.RolResponse;

import java.util.List;

public interface RolService {
    RolResponse crear(RolRequest request);
    List<RolResponse> listar();
    RolResponse actualizar(Long id, RolRequest request);
    void eliminar(Long id);
}