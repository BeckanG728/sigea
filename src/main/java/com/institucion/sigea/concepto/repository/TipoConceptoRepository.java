package com.institucion.sigea.concepto.repository;

import com.institucion.sigea.concepto.entity.TipoConcepto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoConceptoRepository extends JpaRepository<TipoConcepto, Long> {
    boolean existsByNombre(String nombre);
    List<TipoConcepto> findByEstadoTrue();
}