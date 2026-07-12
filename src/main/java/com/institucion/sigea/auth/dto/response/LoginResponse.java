package com.institucion.sigea.auth.dto.response;

public record LoginResponse(
        String token,
        Long expiresIn,
        Long idUsuario,
        String nombreUsuario,
        Long idRol,
        String nombreRol,
        Boolean login2fa
) {
}
