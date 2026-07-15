package com.institucion.sigea.usuario.dto.response;

public record UsuarioResponse(
        Long idUsuario,
        String nombre,
        String primerApellido,
        String numeroDocumento,
        String nombreRol,
        boolean estado,
        Long version
) {}