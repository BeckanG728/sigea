package com.institucion.sigea.alumno.repository;

import com.institucion.sigea.alumno.entity.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
    List<Alumno> findByTipoDocumentoId(Long tipoDocumentoId);
}