package com.institucion.sigea.alumno.repository;

import com.institucion.sigea.alumno.entity.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    List<Alumno> findByTipoDocumentoId(Long tipoDocumentoId);

    Optional<Alumno> findByDniHashAndEstadoTrue(String dniHash);

    List<Alumno> findByEstadoTrue();

    List<Alumno> findByEstadoTrueAndNombresContainingIgnoreCaseOrEstadoTrueAndApellidoPaternoContainingIgnoreCase(
            String nombres, String apellidoPaterno);

    @Query("""
            SELECT a FROM Alumno a
            WHERE a.estado = true
              AND (:q IS NULL OR a.nombres LIKE %:q%
                   OR a.apellidoPaterno LIKE %:q%
                   OR a.apellidoMaterno LIKE %:q%)
            ORDER BY a.apellidoPaterno, a.apellidoMaterno, a.nombres
            """)
    List<Alumno> buscarPorTexto(@Param("q") String q);

}