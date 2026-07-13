package com.institucion.sigea.usuario.service;

import com.institucion.sigea.core.api.PageResponse;
import com.institucion.sigea.core.api.SimpleResponse;
import com.institucion.sigea.usuario.dto.request.ActualizarUsuarioRequest;
import com.institucion.sigea.usuario.dto.request.CrearUsuarioRequest;
import com.institucion.sigea.usuario.dto.response.UsuarioResponse;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {
    SimpleResponse crear(CrearUsuarioRequest request);
    SimpleResponse actualizar(Long id, ActualizarUsuarioRequest request);
    SimpleResponse eliminar(Long id);
    UsuarioResponse obtenerPorId(Long id);
    PageResponse<UsuarioResponse> listar(Pageable pageable);
}
