package com.institucion.sigea.pago.repository;

import com.institucion.sigea.pago.entity.Recibo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReciboRepository extends JpaRepository<Recibo, Integer> {

    Optional<Recibo> findByCodPago(Integer codPago);

    /** Último correlativo emitido en el año, para calcular el siguiente (P3-06). */
    Optional<Recibo> findFirstByAnioOrderByCorrelativoDesc(Integer anio);
}