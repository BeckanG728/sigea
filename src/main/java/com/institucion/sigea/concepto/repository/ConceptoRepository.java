package com.institucion.sigea.concepto.repository;

import com.institucion.sigea.concepto.entity.Concepto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConceptoRepository extends JpaRepository<Concepto, Long> {
    boolean existsByAnioAcademicoIdAndNombreConcepto(Long anioAcademicoId, String nombreConcepto);
    List<Concepto> findByAnioAcademicoId(Long anioAcademicoId);
    Page<Concepto> findByAnioAcademicoIdAndEstadoTrue(Long anioAcademicoId, Pageable pageable);
    Page<Concepto> findByEstadoTrue(Pageable pageable);
}