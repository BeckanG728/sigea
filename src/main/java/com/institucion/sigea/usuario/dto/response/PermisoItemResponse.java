package com.institucion.sigea.usuario.dto.response;

public record PermisoItemResponse(
        Long idFuncionalidad,
        String codigo,
        boolean ver,
        boolean crear,
        boolean editar,
        boolean eliminar,
        boolean imprimir
) {
}
