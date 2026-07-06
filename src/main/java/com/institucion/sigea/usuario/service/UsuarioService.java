package com.institucion.sigea.usuario.service;

import com.institucion.sigea.core.api.PageResponse;
import com.institucion.sigea.usuario.dto.request.ActualizarUsuarioRequest;
import com.institucion.sigea.usuario.dto.request.CrearUsuarioRequest;
import com.institucion.sigea.usuario.dto.response.UsuarioResponse;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {

    UsuarioResponse crear(CrearUsuarioRequest request);

    UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request);

    void eliminar(Long id);

    UsuarioResponse obtenerPorId(Long id);

    PageResponse<UsuarioResponse> listar(Pageable pageable);
}
