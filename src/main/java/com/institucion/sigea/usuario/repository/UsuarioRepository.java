package com.institucion.sigea.usuario.repository;

import com.institucion.sigea.usuario.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String usuario);

    boolean existsByUsername(String usuario);

    @Override
    @EntityGraph(attributePaths = {"rol"})
    Page<Usuario> findAll(Pageable pageable);
}
