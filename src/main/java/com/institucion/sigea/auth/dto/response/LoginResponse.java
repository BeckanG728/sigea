package com.institucion.sigea.auth.dto.response;

public record LoginResponse(
        String token,
        Long expiresIn,
        Long idUsuario,
        String nombreCompleto,
        Long idRol,
        String nombreRol,
        Boolean login2fa
) {
}
