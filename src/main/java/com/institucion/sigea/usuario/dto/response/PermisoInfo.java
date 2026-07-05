package com.institucion.sigea.usuario.dto.response;

public record PermisoInfo(
    Long idFuncionalidad,
    String nombreFuncionalidad,
    boolean ver,
    boolean crear,
    boolean editar,
    boolean eliminar,
    boolean imprimir
) {}
