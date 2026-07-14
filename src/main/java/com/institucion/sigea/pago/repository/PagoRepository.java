package com.institucion.sigea.pago.repository;

import com.institucion.sigea.pago.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    boolean existsByCodCuota(Integer codCuota);
    List<Pago> findByFechaPagoBetween(LocalDateTime desde, LocalDateTime hasta);

    /**
     * Pagos del período con el tipo de concepto de la cuota pagada,
     * para agrupar ingresos por mes y tipo en el reporte de caja.
     */
    @Query("""
    SELECT new com.institucion.sigea.pago.repository.CajaRow(
           p.fechaPago, co.tipoConcepto.nombre, p.montoPagado)
    FROM Pago p, Cuota c, Concepto co
    WHERE p.codCuota = c.id
      AND c.codConcepto = co.id
      AND p.fechaPago BETWEEN :desde AND :hasta
    ORDER BY p.fechaPago ASC
    """)
    List<CajaRow> reporteCaja(@Param("desde") LocalDateTime desde,
                              @Param("hasta") LocalDateTime hasta);
}