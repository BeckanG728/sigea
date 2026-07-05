package com.institucion.sigea.usuario.dto.request;

import jakarta.validation.constraints.NotNull;

public record PermisoItem(
    @NotNull Long idFuncionalidad,
    boolean ver,
    boolean crear,
    boolean editar,
    boolean eliminar,
    boolean imprimir
) {}
