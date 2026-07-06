package com.institucion.sigea.matricula.repository;

import com.institucion.sigea.matricula.entity.Cuota;
import com.institucion.sigea.matricula.entity.EstadoCuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuotaRepository extends JpaRepository<Cuota, Integer> {

    List<Cuota> findByCodMatriculaOrderByOrdenPagoAsc(Integer codMatricula);

    Optional<Cuota> findFirstByCodMatriculaAndEstadoCuotaInOrderByOrdenPagoAsc(
            Integer codMatricula, List<EstadoCuota> estados);

    long countByCodMatriculaAndEstadoCuota(Integer codMatricula, EstadoCuota estadoCuota);
}