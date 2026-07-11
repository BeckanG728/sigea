package com.institucion.sigea.usuario.repository;

import com.institucion.sigea.usuario.entity.RolFuncionalidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolFuncionalidadRepository extends JpaRepository<RolFuncionalidad, Long> {
    List<RolFuncionalidad> findByRolId(Long rolId);
    List<RolFuncionalidad> findByRolIdAndEstadoTrue(Long rolId);
    boolean existsByRolIdAndFuncionalidadId(Long rolId, Long funcionalidadId);
}
