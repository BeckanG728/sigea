package com.institucion.sigea.usuario.dto.request;

public record ActualizarUsuarioRequest(
        String password,
        Long idRol,
        Boolean estado
) {}
