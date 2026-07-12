package com.institucion.sigea.usuario.repository;

import com.institucion.sigea.usuario.entity.Funcionalidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FuncionalidadRepository extends JpaRepository<Funcionalidad, Long> {
    Optional<Funcionalidad> findByNombre(String nombre);
    Optional<Funcionalidad> findByCodigo(String codigo);
    List<Funcionalidad> findByPadreIsNull();
}
