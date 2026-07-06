package com.institucion.sigea.matricula.repository;

import com.institucion.sigea.matricula.entity.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatriculaRepository extends JpaRepository<Matricula, Integer> {

    boolean existsByCodAlumnoAndCodAnioAcademicoAndEstadoTrue(Integer codAlumno, Integer codAnioAcademico);

    Optional<Matricula> findByCodAlumnoAndCodAnioAcademico(Integer codAlumno, Integer codAnioAcademico);

    long countByCodAulaAndCodAnioAcademicoAndEstadoTrue(Integer codAula, Integer codAnioAcademico);
}