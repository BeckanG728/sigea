package com.institucion.sigea.usuario.repository;

import com.institucion.sigea.usuario.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
