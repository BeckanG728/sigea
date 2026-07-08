package com.institucion.sigea.matricula.repository;

import com.institucion.sigea.matricula.entity.Cuota;
import com.institucion.sigea.matricula.entity.EstadoCuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CuotaRepository extends JpaRepository<Cuota, Long> {

    List<Cuota> findByCodMatriculaOrderByOrdenPagoAsc(Integer codMatricula);

    Optional<Cuota> findFirstByCodMatriculaAndEstadoCuotaInOrderByOrdenPagoAsc(
            Integer codMatricula, List<EstadoCuota> estados);

    long countByCodMatriculaAndEstadoCuota(Integer codMatricula, EstadoCuota estadoCuota);

    long countByCodMatriculaAndEstadoCuotaIn(Integer codMatricula, List<EstadoCuota> estados);

    @Query("""
    SELECT c FROM Cuota c, Matricula m
    WHERE c.codMatricula = m.id
      AND m.codAlumno = :codAlumno
      AND c.estadoCuota IN :estados
    ORDER BY m.codAnioAcademico ASC, c.ordenPago ASC
    """)
    List<Cuota> findDeudasPorAlumno(@Param("codAlumno") Integer codAlumno, @Param("estados") List<EstadoCuota> estados);

    List<Cuota> findByCodMatriculaAndEstadoCuotaInAndOrdenPagoLessThanOrderByOrdenPagoAsc(
            Integer codMatricula, List<EstadoCuota> estados, Short ordenPago);

    @Query("""
    SELECT m.codAlumno AS codAlumno,
           SUM(c.montoPagar) AS montoAdeudado,
           COUNT(c) AS cantidadCuotas
    FROM Cuota c, Matricula m
    WHERE c.codMatricula = m.id
      AND c.estadoCuota IN :estados
    GROUP BY m.codAlumno
    ORDER BY SUM(c.montoPagar) DESC
    """)
    List<DeudaAlumnoProjection> reporteDeudasPorAlumno(@Param("estados") List<EstadoCuota> estados);

}