package com.institucion.sigea.usuario.config;

import com.institucion.sigea.usuario.entity.Funcionalidad;
import com.institucion.sigea.usuario.entity.Rol;
import com.institucion.sigea.usuario.entity.RolFuncionalidad;
import com.institucion.sigea.usuario.repository.FuncionalidadRepository;
import com.institucion.sigea.usuario.repository.RolFuncionalidadRepository;
import com.institucion.sigea.usuario.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("!test")
@Order(2) // después de SuperusuarioSeeder
@RequiredArgsConstructor
public class FuncionalidadSeeder implements CommandLineRunner {

    private final FuncionalidadRepository funcionalidadRepository;
    private final RolFuncionalidadRepository rolFuncionalidadRepository;
    private final RolRepository rolRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (funcionalidadRepository.count() > 0) return; // idempotente

        crearArbol("SEGURIDAD", null, List.of("USUARIO", "ROLES", "PERMISOS", "MI_CUENTA"));
        crearArbol("ACADEMICO", null, List.of("AULA", "ALUMNO", "CONCEPTO", "MATRICULA"));
        crearArbol("PAGOS", null, List.of("PAGO"));
        crearArbol("AUDITORIA", null, List.of("AUDITORIA"));
        crearArbol("REPORTES", null, List.of("REPORTE"));

        Rol superusuario = rolRepository.findByNombreRol("SUPERUSUARIO").orElseThrow();
        funcionalidadRepository.findAll().forEach(f ->
                rolFuncionalidadRepository.save(new RolFuncionalidad(superusuario, f, true, true, true, true, true)));

        Funcionalidad miCuenta = funcionalidadRepository.findByNombre("MI_CUENTA").orElseThrow();
        rolRepository.findAll().forEach(rol -> {
            boolean yaTiene = rolFuncionalidadRepository.existsByRolIdAndFuncionalidadId(rol.getId(), miCuenta.getId());
            if (!yaTiene) {
                rolFuncionalidadRepository.save(new RolFuncionalidad(rol, miCuenta, true, false, false, false, false));
            }
        });
    }

    private void crearArbol(String nombrePadre, Funcionalidad padre, List<String> hijos) {
        Funcionalidad p = funcionalidadRepository.findByNombre(nombrePadre)
                .orElseGet(() -> {
                    Funcionalidad nueva = new Funcionalidad();
                    nueva.setNombre(nombrePadre);
                    nueva.setPadre(padre);
                    return funcionalidadRepository.save(nueva);
                });
        hijos.forEach(nombreHijo -> {
            if (funcionalidadRepository.findByNombre(nombreHijo).isEmpty()) {
                Funcionalidad f = new Funcionalidad();
                f.setNombre(nombreHijo);
                f.setPadre(p);
                funcionalidadRepository.save(f);
            }
        });
    }
}