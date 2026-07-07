package com.institucion.sigea.aula.repository;

import com.institucion.sigea.aula.entity.Aula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AulaRepository extends JpaRepository<Aula, Long> {

    boolean existsByAnioAcademicoIdAndNivelIdAndGradoIdAndSeccion(
            Long anioAcademicoId, Long nivelId, Long gradoId, String seccion);

    @Query("""
        SELECT a FROM Aula a
        WHERE a.estado = true
          AND (:anioAcademicoId IS NULL OR a.anioAcademico.id = :anioAcademicoId)
          AND (:nivelId IS NULL OR a.nivel.id = :nivelId)
        """)
    List<Aula> buscar(@Param("anioAcademicoId") Long anioAcademicoId, @Param("nivelId") Long nivelId);
}