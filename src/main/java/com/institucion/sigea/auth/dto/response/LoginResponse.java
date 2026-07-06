package com.institucion.sigea.auth.dto.response;

public record LoginResponse(
        String token,
        String tokenType,
        Long expiresIn,
        Long idUsuario,
        String rol,
        boolean requiere2FA
) {

    public static LoginResponse withToken(String token, long expiresIn,
                                          Long idUsuario, String rol) {
        return new LoginResponse(token, "Bearer", expiresIn, idUsuario, rol, false);
    }

    public static LoginResponse requires2fa(Long idUsuario, String rol) {
        return new LoginResponse(null, null, null, idUsuario, rol, true);
    }
}
