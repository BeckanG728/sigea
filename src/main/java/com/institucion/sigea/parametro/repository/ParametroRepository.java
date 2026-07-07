package com.institucion.sigea.parametro.repository;

import com.institucion.sigea.parametro.entity.Parametro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ParametroRepository extends JpaRepository<Parametro, Long> {
    Optional<Parametro> findByClave(String clave);
}