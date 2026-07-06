package com.institucion.sigea.aula.repository;

import com.institucion.sigea.aula.entity.Aula;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AulaRepository extends JpaRepository<Aula, Long> {
    boolean existsByAnioAcademicoIdAndNivelIdAndGradoIdAndSeccion(
            Long anioAcademicoId, Long nivelId, Long gradoId, String seccion);
}