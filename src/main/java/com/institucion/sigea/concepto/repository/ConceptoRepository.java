package com.institucion.sigea.concepto.repository;

import com.institucion.sigea.concepto.entity.Concepto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConceptoRepository extends JpaRepository<Concepto, Long> {
    boolean existsByAnioAcademicoIdAndNombreConcepto(Long anioAcademicoId, String nombreConcepto);
    List<Concepto> findByAnioAcademicoId(Long anioAcademicoId); // la usarás en P2-07 (clonado)
}