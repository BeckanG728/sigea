package com.institucion.sigea.usuario.dto.response;

import java.util.List;

public record MisPermisosWrapper(
        List<MisPermisosResponse> permisos
) {}
