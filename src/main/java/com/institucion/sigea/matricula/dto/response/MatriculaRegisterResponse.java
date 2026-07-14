package com.institucion.sigea.matricula.dto.response;

import java.time.LocalDate;
import java.util.List;

public record MatriculaRegisterResponse(
        boolean exito,
        String error,
        MatriculaRegistrada matricula,
        List<ObligacionResponse> obligaciones
) {
    public static MatriculaRegisterResponse exito(MatriculaRegistrada m, List<ObligacionResponse> obligaciones) {
        return new MatriculaRegisterResponse(true, null, m, obligaciones);
    }

    public static MatriculaRegisterResponse error(String mensaje) {
        return new MatriculaRegisterResponse(false, mensaje, null, List.of());
    }

    public record MatriculaRegistrada(
            Long id,
            Long alumnoId,
            Long aulaId,
            Long anioId,
            LocalDate fecha,
            String estado,
            Long usuarioId
    ) {}
}