package com.institucion.sigea.usuario.dto.response;

import java.util.List;

public record MisPermisosResponse(
        Long idFuncionalidad,
        String nombre,
        PermisosFlags permisos,
        List<MisPermisosResponse> hijos
) {
    public record PermisosFlags(boolean ver, boolean crear, boolean editar, boolean eliminar, boolean imprimir) {}
}