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

        Funcionalidad seguridad = crearFuncionalidad("SEGURIDAD", "Seguridad", null);
        Funcionalidad academico = crearFuncionalidad("ACADEMICO", "Académico", null);
        Funcionalidad pagos = crearFuncionalidad("PAGOS", "Pagos", null);
        Funcionalidad auditoria = crearFuncionalidad("AUDITORIA", "Auditoría", null);
        Funcionalidad reportes = crearFuncionalidad("REPORTES", "Reportes", null);

        crearFuncionalidad("USUARIOS", "Usuarios", seguridad);
        crearFuncionalidad("ROLES", "Roles", seguridad);
        crearFuncionalidad("PARAMETROS", "Parámetros", seguridad);
        crearFuncionalidad("MI_CUENTA", "Mi Cuenta", seguridad);

        crearFuncionalidad("AULAS", "Aulas", academico);
        crearFuncionalidad("ALUMNOS", "Alumnos", academico);
        crearFuncionalidad("CONCEPTOS", "Conceptos", academico);
        crearFuncionalidad("MATRICULA_REGISTRAR", "Registrar Matrícula", academico);

        crearFuncionalidad("PAGO_REGISTRAR", "Registrar Pago", pagos);
        crearFuncionalidad("DEUDA_HISTORIAL", "Historial de Deudas", pagos);

        crearFuncionalidad("AUDITORIA_REGISTRO", "Registro de Auditoría", auditoria);

        crearFuncionalidad("REPORTE_MATRICULA", "Reporte de Matrícula", reportes);
        crearFuncionalidad("REPORTE_VACANTES", "Reporte de Vacantes", reportes);
        crearFuncionalidad("REPORTE_DEUDAS", "Reporte de Deudas", reportes);
        crearFuncionalidad("REPORTE_CAJA", "Reporte de Caja", reportes);

        Rol superusuario = rolRepository.findByNombre("SUPERUSUARIO").orElseThrow();
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

    private Funcionalidad crearFuncionalidad(String codigo, String nombre, Funcionalidad padre) {
        return funcionalidadRepository.findByCodigo(codigo)
                .orElseGet(() -> {
                    Funcionalidad f = new Funcionalidad();
                    f.setCodigo(codigo);
                    f.setNombre(nombre);
                    f.setPadre(padre);
                    return funcionalidadRepository.save(f);
                });
    }
}