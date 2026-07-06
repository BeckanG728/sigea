package com.institucion.sigea.usuario.dto.response;

import java.time.Instant;

public record UsuarioResponse(
        Long idUsuario,
        String usuario,
        Long idRol,
        String nombreRol,
        boolean estado,
        boolean dosFactorHabilitado,
        Instant fechaRegistro
) {
}
