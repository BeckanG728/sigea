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


@Component
@Profile("!test")
@Order(2)
@RequiredArgsConstructor
public class FuncionalidadSeeder implements CommandLineRunner {

    private final FuncionalidadRepository funcionalidadRepository;
    private final RolFuncionalidadRepository rolFuncionalidadRepository;
    private final RolRepository rolRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (funcionalidadRepository.count() > 0) return;

        Funcionalidad seguridad = crearFuncionalidad("Seguridad", null);
        Funcionalidad academico = crearFuncionalidad("Académico", null);
        Funcionalidad pagos = crearFuncionalidad("Pagos", null);
        Funcionalidad auditoria = crearFuncionalidad("Auditoría", null);
        Funcionalidad reportes = crearFuncionalidad("Reportes", null);

        crearFuncionalidad("Usuarios", seguridad);
        crearFuncionalidad("Roles", seguridad);
        crearFuncionalidad("Permisos", seguridad);
        crearFuncionalidad("Parámetros", seguridad);
        crearFuncionalidad("Mi Cuenta", seguridad);

        crearFuncionalidad("Aulas", academico);
        crearFuncionalidad("Alumnos", academico);
        crearFuncionalidad("Conceptos", academico);
        crearFuncionalidad("Registrar Matrícula", academico);

        crearFuncionalidad("Registrar Pago", pagos);
        crearFuncionalidad("Historial de Deudas", pagos);

        crearFuncionalidad("Registro de Auditoría", auditoria);

        crearFuncionalidad("Reporte de Matrícula", reportes);
        crearFuncionalidad("Reporte de Vacantes", reportes);
        crearFuncionalidad("Reporte de Deudas", reportes);
        crearFuncionalidad("Reporte de Caja", reportes);

        Rol superusuario = rolRepository.findByNombreRol("SUPERUSUARIO").orElseThrow();
        funcionalidadRepository.findAll().forEach(f ->
                rolFuncionalidadRepository.save(new RolFuncionalidad(superusuario, f, true, true, true, true, true)));

        Funcionalidad miCuenta = funcionalidadRepository.findByNombre("Mi Cuenta").orElseThrow();
        rolRepository.findAll().forEach(rol -> {
            boolean yaTiene = rolFuncionalidadRepository.existsByRolIdAndFuncionalidadId(rol.getId(), miCuenta.getId());
            if (!yaTiene) {
                rolFuncionalidadRepository.save(new RolFuncionalidad(rol, miCuenta, true, false, false, false, false));
            }
        });
    }

    private Funcionalidad crearFuncionalidad(String nombre, Funcionalidad padre) {
        return funcionalidadRepository.findByNombre(nombre)
                .orElseGet(() -> {
                    Funcionalidad f = new Funcionalidad();
                    f.setNombre(nombre);
                    f.setPadre(padre);
                    return funcionalidadRepository.save(f);
                });
    }
}