package com.institucion.sigea.matricula.repository;

import java.math.BigDecimal;

public interface DeudaAlumnoProjection {
    Integer getCodAlumno();
    BigDecimal getMontoAdeudado();
    Long getCantidadCuotas();
}
