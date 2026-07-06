package com.institucion.sigea.pago.repository;

import com.institucion.sigea.pago.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    boolean existsByCodCuota(Integer codCuota);
}