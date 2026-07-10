package com.institucion.sigea.aula.repository;

import com.institucion.sigea.aula.entity.AnioAcademico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnioAcademicoRepository extends JpaRepository<AnioAcademico,Long> {
    Optional<AnioAcademico> findByEstadoTrue();
}
