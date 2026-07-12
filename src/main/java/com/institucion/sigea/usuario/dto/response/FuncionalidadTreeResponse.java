package com.institucion.sigea.usuario.dto.response;

import java.util.List;

public record FuncionalidadTreeResponse(
        Long idFuncionalidad,
        String nombre,
        String codigo,
        List<FuncionalidadTreeResponse> hijos
) {
}
