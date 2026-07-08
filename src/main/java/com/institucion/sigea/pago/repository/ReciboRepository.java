package com.institucion.sigea.pago.repository;

import com.institucion.sigea.pago.entity.Recibo;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface ReciboRepository extends JpaRepository<Recibo, Long> {

    Optional<Recibo> findByCodPago(Integer codPago);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Recibo> findFirstByAnioOrderByCorrelativoDesc(Integer anio);
}