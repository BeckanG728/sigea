package com.institucion.sigea.pago.repository;

import com.institucion.sigea.pago.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    boolean existsByCodCuota(Integer codCuota);
    List<Pago> findByFechaPagoBetween(LocalDateTime desde, LocalDateTime hasta);
}