package com.institucion.sigea.aula.repository;

import com.institucion.sigea.aula.entity.Aula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface AulaRepository extends JpaRepository<Aula, Long> {

    boolean existsByAnioAcademicoIdAndNivelIdAndGradoIdAndSeccion(
            Long anioAcademicoId, Long nivelId, Long gradoId, String seccion);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT a FROM Aula a WHERE a.id = :id")
    Optional<Aula> findWithLockById(@Param("id") Long id);

    @Query("""
        SELECT a FROM Aula a
        WHERE a.estado = true
          AND (:anioAcademicoId IS NULL OR a.anioAcademico.id = :anioAcademicoId)
          AND (:nivelId IS NULL OR a.nivel.id = :nivelId)
        """)
    List<Aula> buscar(@Param("anioAcademicoId") Long anioAcademicoId, @Param("nivelId") Long nivelId);

    @Query("""
    SELECT a FROM Aula a
    WHERE (:anioAcademicoId IS NULL OR a.anioAcademico.id = :anioAcademicoId)
      AND (:nivelId IS NULL OR a.nivel.id = :nivelId)
    ORDER BY a.anioAcademico.anio, a.nivel.nombre, a.grado.nombreGrado, a.seccion
    """)
    List<Aula> buscarTodos(@Param("anioAcademicoId") Long anioAcademicoId, @Param("nivelId") Long nivelId);
}