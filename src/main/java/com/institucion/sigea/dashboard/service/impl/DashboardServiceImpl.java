package com.institucion.sigea.dashboard.service.impl;

import com.institucion.sigea.alumno.entity.Alumno;
import com.institucion.sigea.alumno.repository.AlumnoRepository;
import com.institucion.sigea.aula.entity.Aula;
import com.institucion.sigea.aula.repository.AulaRepository;
import com.institucion.sigea.core.api.PageResponse;
import com.institucion.sigea.dashboard.dto.DashboardResponse;
import com.institucion.sigea.dashboard.dto.MatriculaDashboardResponse;
import com.institucion.sigea.dashboard.service.DashboardService;
import com.institucion.sigea.matricula.entity.EstadoCuota;
import com.institucion.sigea.matricula.entity.Matricula;
import com.institucion.sigea.matricula.repository.CuotaRepository;
import com.institucion.sigea.matricula.repository.MatriculaRepository;
import com.institucion.sigea.usuario.entity.Usuario;
import com.institucion.sigea.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MatriculaRepository matriculaRepository;
    private final CuotaRepository cuotaRepository;
    private final AulaRepository aulaRepository;
    private final AlumnoRepository alumnoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse obtenerResumen(Pageable pageable) {
        long totalMatriculas = matriculaRepository.count();
        long totalAulasActivas = aulaRepository.countByEstadoTrue();
        long pagosPendientes = cuotaRepository.countByEstadoCuota(EstadoCuota.PENDIENTE);

        Page<Matricula> page = matriculaRepository.findAllByOrderByFechaMatriculaDesc(pageable);
        Map<Long, Alumno> alumnos = cargarAlumnos(page);
        Map<Long, Aula> aulas = cargarAulas(page);
        Map<Long, Usuario> usuarios = cargarUsuarios(page);

        PageResponse<MatriculaDashboardResponse> matriculas = PageResponse.of(
                page.map(m -> MatriculaDashboardResponse.from(
                        m,
                        alumnos.get(m.getCodAlumno().longValue()),
                        aulas.get(m.getCodAula().longValue()),
                        usuarios.get(m.getCodUsuario().longValue()),
                        page.getNumber() * page.getSize() + page.getContent().indexOf(m) + 1
                ))
        );

        return new DashboardResponse(totalMatriculas, totalAulasActivas, pagosPendientes, matriculas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaDashboardResponse> exportarMatriculas() {
        List<Matricula> todas = matriculaRepository.findAllByOrderByFechaMatriculaDesc();

        Map<Long, Alumno> alumnos = todas.stream()
                .collect(Collectors.toMap(
                        m -> m.getCodAlumno().longValue(),
                        m -> alumnoRepository.findById(m.getCodAlumno().longValue()).orElse(null),
                        (a, b) -> a
                ));
        Map<Long, Aula> aulas = todas.stream()
                .collect(Collectors.toMap(
                        m -> m.getCodAula().longValue(),
                        m -> aulaRepository.findById(m.getCodAula().longValue()).orElse(null),
                        (a, b) -> a
                ));
        Map<Long, Usuario> usuarios = todas.stream()
                .collect(Collectors.toMap(
                        m -> m.getCodUsuario().longValue(),
                        m -> usuarioRepository.findById(m.getCodUsuario().longValue()).orElse(null),
                        (a, b) -> a
                ));

        long[] i = {1};
        return todas.stream()
                .map(m -> MatriculaDashboardResponse.from(
                        m,
                        alumnos.get(m.getCodAlumno().longValue()),
                        aulas.get(m.getCodAula().longValue()),
                        usuarios.get(m.getCodUsuario().longValue()),
                        i[0]++
                ))
                .toList();
    }

    private Map<Long, Alumno> cargarAlumnos(Page<Matricula> page) {
        return page.getContent().stream()
                .map(m -> alumnoRepository.findById(m.getCodAlumno().longValue()).orElse(null))
                .filter(a -> a != null)
                .collect(Collectors.toMap(Alumno::getId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, Aula> cargarAulas(Page<Matricula> page) {
        return page.getContent().stream()
                .map(m -> aulaRepository.findById(m.getCodAula().longValue()).orElse(null))
                .filter(a -> a != null)
                .collect(Collectors.toMap(Aula::getId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, Usuario> cargarUsuarios(Page<Matricula> page) {
        return page.getContent().stream()
                .map(m -> usuarioRepository.findById(m.getCodUsuario().longValue()).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toMap(Usuario::getId, Function.identity(), (a, b) -> a));
    }
}
