package com.institucion.sigea.aula.service.impl;

import com.institucion.sigea.alumno.entity.Alumno;
import com.institucion.sigea.alumno.repository.AlumnoRepository;
import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.aula.dto.request.AulaRequest;
import com.institucion.sigea.aula.dto.response.AlumnoAulaResponse;
import com.institucion.sigea.aula.dto.response.AulaBusquedaResponse;
import com.institucion.sigea.aula.dto.response.AulaListadoResponse;
import com.institucion.sigea.aula.dto.response.AulaMatriculaResponse;
import com.institucion.sigea.aula.dto.response.AulaResponse;
import com.institucion.sigea.aula.entity.Aula;
import com.institucion.sigea.aula.mapper.AulaMapper;
import com.institucion.sigea.aula.repository.AnioAcademicoRepository;
import com.institucion.sigea.aula.repository.AulaRepository;
import com.institucion.sigea.aula.repository.GradoRepository;
import com.institucion.sigea.aula.repository.NivelRepository;
import com.institucion.sigea.aula.service.AulaService;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.matricula.repository.MatriculaRepository;
import com.institucion.sigea.parametro.service.ParametroService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AulaServiceImpl implements AulaService {
    private static final String FORMATO_CODIGO_AULA = "AU-%03d";

    private final AulaRepository aulaRepository;
    private final AnioAcademicoRepository anioAcademicoRepository;
    private final NivelRepository nivelRepository;
    private final GradoRepository gradoRepository;
    private final ParametroService parametroService;
    private final MatriculaRepository matriculaRepository;
    private final AlumnoRepository alumnoRepository;
    private final AulaMapper aulaMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    @Auditable(modulo = "aula", operacion = TipoOperacionAuditoria.INSERT)
    public AulaResponse crear(AulaRequest request) {
        boolean duplicada = aulaRepository.existsByAnioAcademicoIdAndNivelIdAndGradoIdAndSeccion(
                request.codAnioAcademico(), request.codNivel(), request.codGrado(), request.seccion());
        if (duplicada) {
            throw new BusinessException(ErrorCode.AULA_DUPLICADA, "Aula ya registrada",
                    Map.of("seccion", request.seccion()));
        }

        short capacidad = request.capacidadMaxima() != null
                ? request.capacidadMaxima()
                : Short.parseShort(parametroService.obtener("VACANTES_MAXIMAS_DEFAULT"));

        Aula aula = new Aula();
        aula.setAnioAcademico(anioAcademicoRepository.getReferenceById(request.codAnioAcademico()));
        aula.setNivel(nivelRepository.getReferenceById(request.codNivel()));
        aula.setGrado(gradoRepository.getReferenceById(request.codGrado()));
        aula.setSeccion(request.seccion());
        aula.setCapacidadMaxima(capacidad);
        Long siguienteCorrelativo = ((Number) entityManager
                .createNativeQuery("SELECT nextval('seq_codigo_aula')")
                .getSingleResult()).longValue();
        aula.setCodigo(FORMATO_CODIGO_AULA.formatted(siguienteCorrelativo));
        aulaRepository.save(aula);
        return aulaMapper.toResponse(aula);
    }

    @Override
    public List<AulaBusquedaResponse> buscar(Long anioAcademicoId, Long nivelId) {
        return aulaRepository.buscar(anioAcademicoId, nivelId).stream()
                .map(aulaMapper::toBusquedaResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AulaMatriculaResponse> buscarParaMatricula(Integer periodo) {
        return anioAcademicoRepository.findAll().stream()
                .filter(a -> a.getAnio().equals(periodo))
                .findFirst()
                .map(anio -> aulaRepository.buscar(anio.getId(), null).stream()
                        .map(aula -> {
                            long matriculados = matriculaRepository
                                    .countByCodAulaAndCodAnioAcademicoAndEstadoTrue(
                                            aula.getId().intValue(), periodo);
                            return new AulaMatriculaResponse(
                                    aula.getId(),
                                    aula.getNivel().getNombre(),
                                    aula.getGrado().getNombreGrado(),
                                    aula.getSeccion(),
                                    matriculados,
                                    aula.getCapacidadMaxima(),
                                    aula.isEstado(),
                                    periodo
                            );
                        })
                        .toList())
                .orElse(List.of());
    }

    @Override
    public List<AulaListadoResponse> listarConDetalle(Long anioAcademicoId, Long nivelId) {
        return aulaRepository.buscarTodos(anioAcademicoId, nivelId).stream()
                .map(aula -> {
                    long matriculados = matriculaRepository
                            .countByCodAulaAndCodAnioAcademicoAndEstadoTrue(
                                    aula.getId().intValue(),
                                    aula.getAnioAcademico().getAnio());
                    AulaListadoResponse dto = aulaMapper.toListadoResponse(aula);
                    return new AulaListadoResponse(
                            dto.id(), dto.codigo(), dto.nivel(), dto.grado(),
                            dto.seccion(), dto.capacidadMaxima(),
                            matriculados,
                            dto.capacidadMaxima() - matriculados,
                            aula.isEstado()
                    );
                })
                .toList();
    }

    @Override
    public AulaListadoResponse obtenerPorId(Long id) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.AULA_NO_ENCONTRADA, "Aula no encontrada"));
        long matriculados = matriculaRepository
                .countByCodAulaAndCodAnioAcademicoAndEstadoTrue(
                        aula.getId().intValue(),
                        aula.getAnioAcademico().getAnio());
        AulaListadoResponse dto = aulaMapper.toListadoResponse(aula);
        return new AulaListadoResponse(
                dto.id(), dto.codigo(), dto.nivel(), dto.grado(),
                dto.seccion(), dto.capacidadMaxima(),
                matriculados,
                dto.capacidadMaxima() - matriculados,
                aula.isEstado()
        );
    }

    @Override
    public List<AlumnoAulaResponse> obtenerAlumnos(Long aulaId, Long anioAcademicoId) {
        return matriculaRepository.buscarParaReporte(
                        anioAcademicoId != null ? anioAcademicoId.intValue() : null,
                        null, null, aulaId.intValue()
                ).stream().map(m -> {
                    Alumno alumno = alumnoRepository.findById(m.getCodAlumno().longValue())
                            .orElse(null);
                    if (alumno == null) return null;
                    return new AlumnoAulaResponse(
                            alumno.getCodigo(),
                            alumno.getNombres() + " " + alumno.getApellidoPaterno() + " " + alumno.getApellidoMaterno(),
                            m.getFechaMatricula().toString(),
                            m.isEstado()
                    );
                }).filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional
    @Auditable(modulo = "aula", operacion = TipoOperacionAuditoria.UPDATE)
    public AulaResponse actualizar(Long id, AulaRequest request) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.AULA_NO_ENCONTRADA, "Aula no encontrada"));

        boolean duplicada = aulaRepository.existsByAnioAcademicoIdAndNivelIdAndGradoIdAndSeccion(
                request.codAnioAcademico(), request.codNivel(), request.codGrado(), request.seccion());
        if (duplicada && !aula.getSeccion().equals(request.seccion())) {
            throw new BusinessException(ErrorCode.AULA_DUPLICADA, "Aula ya registrada",
                    Map.of("seccion", request.seccion()));
        }

        aula.setNivel(nivelRepository.getReferenceById(request.codNivel()));
        aula.setGrado(gradoRepository.getReferenceById(request.codGrado()));
        aula.setSeccion(request.seccion());
        if (request.capacidadMaxima() != null) {
            aula.setCapacidadMaxima(request.capacidadMaxima());
        }
        aulaRepository.save(aula);
        return aulaMapper.toResponse(aula);
    }

    @Override
    @Transactional
    @Auditable(modulo = "aula", operacion = TipoOperacionAuditoria.DELETE)
    public void eliminar(Long id) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.AULA_NO_ENCONTRADA, "Aula no encontrada"));
        aula.setEstado(!aula.isEstado());
        aulaRepository.save(aula);
    }
}