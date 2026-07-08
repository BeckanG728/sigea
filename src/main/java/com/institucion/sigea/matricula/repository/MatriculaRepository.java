package com.institucion.sigea.matricula.repository;

import com.institucion.sigea.matricula.entity.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    boolean existsByCodAlumnoAndCodAnioAcademicoAndEstadoTrue(Integer codAlumno, Integer codAnioAcademico);

    Optional<Matricula> findByCodAlumnoAndCodAnioAcademico(Integer codAlumno, Integer codAnioAcademico);

    long countByCodAulaAndCodAnioAcademicoAndEstadoTrue(Integer codAula, Integer codAnioAcademico);

    List<Matricula> findByCodAlumnoAndEstadoTrueAndCodAnioAcademicoNot(Integer codAlumno, Integer codAnioAcademico);

    @Query("""
    SELECT m FROM Matricula m, Aula a
    WHERE m.codAula = a.id
      AND m.estado = true
      AND (:anioAcademico IS NULL OR m.codAnioAcademico = :anioAcademico)
      AND (:codNivel IS NULL OR a.nivel.id = :codNivel)
      AND (:codGrado IS NULL OR a.grado.id = :codGrado)
      AND (:codAula IS NULL OR m.codAula = :codAula)
    ORDER BY m.fechaMatricula DESC
    """)
    List<Matricula> buscarParaReporte(
            @Param("anioAcademico") Integer anioAcademico,
            @Param("codNivel") Long codNivel,
            @Param("codGrado") Long codGrado,
            @Param("codAula") Integer codAula);
}