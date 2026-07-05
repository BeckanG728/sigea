package com.institucion.sigea.usuario.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PermisoRequest(
    @NotEmpty(message = "La lista de permisos no puede estar vacia")
    @Valid
    List<PermisoItem> permisos
) {}
