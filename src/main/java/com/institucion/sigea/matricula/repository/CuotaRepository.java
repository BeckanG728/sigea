package com.institucion.sigea.matricula.repository;

import com.institucion.sigea.matricula.entity.Cuota;
import com.institucion.sigea.matricula.entity.EstadoCuota;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CuotaRepository extends JpaRepository<Cuota, Long> {

    long countByEstadoCuota(EstadoCuota estadoCuota);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cuota c WHERE c.id = :id")
    Optional<Cuota> findWithLockById(@Param("id") Long id);

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

    @Query("""
    SELECT c FROM Cuota c, Matricula m
    WHERE c.codMatricula = m.id
      AND m.codAlumno = :codAlumno
    ORDER BY c.ordenPago ASC
    """)
    Page<Cuota> findCuotasPorAlumno(@Param("codAlumno") Integer codAlumno, Pageable pageable);

    @Query("""
    SELECT c FROM Cuota c, Matricula m
    WHERE c.codMatricula = m.id
      AND m.codAlumno = :codAlumno
    ORDER BY c.ordenPago ASC
    """)
    List<Cuota> findTodasCuotasPorAlumno(@Param("codAlumno") Integer codAlumno);

    List<Cuota> findByCodMatriculaAndEstadoCuotaInOrderByOrdenPagoAsc(
            Integer codMatricula, List<EstadoCuota> estados);

    List<Cuota> findByCodMatriculaAndEstadoCuotaInAndOrdenPagoLessThanOrderByOrdenPagoAsc(
            Integer codMatricula, List<EstadoCuota> estados, Short ordenPago);

    @Query("""
    SELECT c FROM Cuota c, Matricula m
    WHERE c.codMatricula = m.id
      AND c.estadoCuota IN :estados
""")
    Page<Cuota> findAllDeudas(@Param("estados") List<EstadoCuota> estados, Pageable pageable);

    @Query("""
    SELECT COUNT(DISTINCT m.codAlumno)
    FROM Cuota c, Matricula m
    WHERE c.codMatricula = m.id
      AND c.estadoCuota IN :estados
""")
    long contarAlumnosDeudores(@Param("estados") List<EstadoCuota> estados);

    @Query("""
    SELECT COALESCE(SUM(c.montoPagar), 0)
    FROM Cuota c, Matricula m
    WHERE c.codMatricula = m.id
      AND c.estadoCuota IN :estados
""")
    BigDecimal sumarTotalDeuda(@Param("estados") List<EstadoCuota> estados);

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

    /**
     * Reporte detallado de deudas: una fila por cuota adeudada, con los datos
     * del alumno, su documento y el concepto asociado.
     * El converter AES de numeroDocumento se aplica automáticamente al leer.
     */
    @Query("""
    SELECT new com.institucion.sigea.matricula.repository.DeudaDetalleRow(
           c.id, a.nombres, a.apellidoPaterno, a.apellidoMaterno,
           a.tipoDocumento.descripcion, a.numeroDocumento, co.nombreConcepto,
           co.anioAcademico.anio, c.ordenPago, c.montoPagar, c.estadoCuota)
    FROM Cuota c, Matricula m, Alumno a, Concepto co
    WHERE c.codMatricula = m.id
      AND m.codAlumno = a.id
      AND c.codConcepto = co.id
      AND c.estadoCuota IN :estados
      AND (:anio IS NULL OR co.anioAcademico.anio = :anio)
    ORDER BY a.apellidoPaterno ASC, a.nombres ASC, c.ordenPago ASC
    """)
    List<DeudaDetalleRow> reporteDeudasDetalle(@Param("estados") List<EstadoCuota> estados,
                                               @Param("anio") Integer anio);
}

