package com.institucion.sigea.usuario.service;

import com.institucion.sigea.usuario.dto.request.PermisoItem;
import com.institucion.sigea.usuario.dto.response.PermisoInfo;
import java.util.List;

public interface PermisoService {
    void aplicar(Long idRol, List<PermisoItem> permisos);
    List<PermisoInfo> obtenerPermisos(Long idRol);
}
