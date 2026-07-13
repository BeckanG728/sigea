package com.institucion.sigea.usuario.dto.request;

public record PermisoItem(
    Long idFuncionalidad,
    String codigo,
    boolean ver,
    boolean crear,
    boolean editar,
    boolean eliminar,
    boolean imprimir
) {}
