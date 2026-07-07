package com.institucion.sigea.pago.repository;

import com.institucion.sigea.pago.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    boolean existsByCodCuota(Integer codCuota);
}