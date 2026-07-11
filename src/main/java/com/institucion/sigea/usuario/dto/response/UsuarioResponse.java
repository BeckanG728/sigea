package com.institucion.sigea.usuario.dto.response;

import java.time.Instant;

public record UsuarioResponse(
        Long idUsuario,
        String codigo,
        String usuario,
        String nombre,
        String primerApellido,
        String numeroDocumento,
        Long idRol,
        String nombreRol,
        boolean estado,
        boolean dosFactorHabilitado,
        boolean totpVerificado,
        Instant fechaRegistro
) {}
