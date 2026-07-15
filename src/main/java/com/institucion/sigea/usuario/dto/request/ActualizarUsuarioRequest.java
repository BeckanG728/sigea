package com.institucion.sigea.usuario.dto.request;

public record ActualizarUsuarioRequest(
        String nombre,
        String primerApellido,
        String numeroDocumento,
        Long idRol,
        Long version
) {}