package com.institucion.sigea.alumno.service.impl;

import com.institucion.sigea.alumno.dto.request.AlumnoRequest;
import com.institucion.sigea.alumno.dto.response.AlumnoBusquedaResponse;
import com.institucion.sigea.alumno.dto.response.AlumnoMatriculaResponse;
import com.institucion.sigea.alumno.dto.response.AlumnoResponse;
import com.institucion.sigea.alumno.entity.Alumno;
import com.institucion.sigea.alumno.entity.TipoDocumento;
import com.institucion.sigea.alumno.mapper.AlumnoMapper;
import com.institucion.sigea.alumno.repository.AlumnoRepository;
import com.institucion.sigea.alumno.repository.TipoDocumentoRepository;
import com.institucion.sigea.alumno.service.AlumnoService;
import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.concepto.entity.Concepto;
import com.institucion.sigea.concepto.repository.ConceptoRepository;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.matricula.entity.EstadoCuota;
import com.institucion.sigea.matricula.entity.Matricula;
import com.institucion.sigea.matricula.repository.CuotaRepository;
import com.institucion.sigea.matricula.repository.MatriculaRepository;
import com.institucion.sigea.pago.dto.response.DeudaMatriculaResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlumnoServiceImpl implements AlumnoService {
    private static final String FORMATO_CODIGO_ALUMNO = "AL-%04d";

    private final AlumnoRepository alumnoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final AlumnoMapper alumnoMapper;
    private final EntityManager entityManager;
    private final MatriculaRepository matriculaRepository;
    private final CuotaRepository cuotaRepository;
    private final ConceptoRepository conceptoRepository;

    @Override
    @Transactional
    @Auditable(modulo = "alumno", operacion = TipoOperacionAuditoria.INSERT)
    public AlumnoResponse crear(AlumnoRequest request) {
        List<Alumno> candidatos = alumnoRepository.findByTipoDocumentoId(request.codTipoDocumento());
        boolean duplicado = candidatos.stream()
                .anyMatch(a -> a.getNumeroDocumento().equals(request.numeroDocumento()));
        if (duplicado) {
            throw new BusinessException(ErrorCode.ALUMNO_DUPLICADO,
                    "Ya existe un alumno con ese documento",
                    Map.of("numeroDocumento", request.numeroDocumento()));
        }

        TipoDocumento tipoDocumento = tipoDocumentoRepository.getReferenceById(request.codTipoDocumento());

        Alumno alumno = new Alumno();
        alumno.setTipoDocumento(tipoDocumento);
        alumno.setNumeroDocumento(request.numeroDocumento());
        alumno.setNombres(request.nombres());
        alumno.setApellidoPaterno(request.apellidoPaterno());
        alumno.setApellidoMaterno(request.apellidoMaterno());
        alumno.setFechaNacimiento(request.fechaNacimiento().toString());
        Long siguienteCorrelativo = ((Number) entityManager
                .createNativeQuery("SELECT nextval('seq_codigo_alumno')")
                .getSingleResult()).longValue();
        alumno.setCodigo(FORMATO_CODIGO_ALUMNO.formatted(siguienteCorrelativo));
        alumnoRepository.save(alumno);
        return alumnoMapper.toResponse(alumno);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeudaMatriculaResponse> listarDeudasMatricula(Long alumnoId, Integer anio) {
        List<Matricula> matriculasAnteriores = matriculaRepository
                .findByCodAlumnoAndEstadoTrueAndCodAnioAcademicoNot(
                        alumnoId.intValue(), anio);

        Map<Integer, String> conceptosMap = conceptoRepository.findAll().stream()
                .collect(Collectors.toMap(
                        c -> c.getId().intValue(),
                        Concepto::getNombreConcepto));

        return matriculasAnteriores.stream()
                .flatMap(m -> cuotaRepository
                        .findByCodMatriculaAndEstadoCuotaInOrderByOrdenPagoAsc(
                                m.getId().intValue(),
                                List.of(EstadoCuota.PENDIENTE, EstadoCuota.BLOQUEADA))
                        .stream()
                        .map(c -> new DeudaMatriculaResponse(
                                conceptosMap.getOrDefault(c.getCodConcepto(), "Concepto"),
                                c.getMontoPagar(),
                                c.getFechaVencimiento() != null
                                        ? c.getFechaVencimiento()
                                        : (c.getFechaPago() != null
                                           ? c.getFechaPago().toLocalDate()
                                           : null),
                                c.getEstadoCuota().name(),
                                c.getNumeroRecibo()
                        )))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumnoMatriculaResponse> buscarParaMatricula(String q) {
        if (q == null || q.isBlank()) {
            return alumnoRepository.findByEstadoTrue().stream()
                    .map(this::toMatriculaResponse)
                    .toList();
        }

        List<Alumno> porTexto = alumnoRepository.buscarPorTexto(q.trim());

        List<Alumno> porDoc = alumnoRepository.buscarPorDocumento(q.trim());

        Set<Alumno> combinados = new LinkedHashSet<>();
        combinados.addAll(porTexto);
        combinados.addAll(porDoc);

        return combinados.stream()
                .map(this::toMatriculaResponse)
                .toList();
    }

    private AlumnoMatriculaResponse toMatriculaResponse(Alumno a) {
        return new AlumnoMatriculaResponse(
                a.getId(),
                a.getNumeroDocumento(),
                a.getApellidoPaterno(),
                a.getApellidoMaterno(),
                a.getNombres(),
                a.isEstado()
        );
    }

    @Override
    public List<AlumnoBusquedaResponse> buscar(String nombres) {
        List<Alumno> alumnos = (nombres == null || nombres.isBlank())
                ? alumnoRepository.findByEstadoTrue()
                : alumnoRepository.findByEstadoTrueAndNombresContainingIgnoreCaseOrEstadoTrueAndApellidoPaternoContainingIgnoreCase(
                nombres, nombres);

        return alumnos.stream()
                .map(alumnoMapper::toBusquedaResponse)
                .toList();
    }

    private AlumnoResponse toResponse(Alumno alumno) {
        return new AlumnoResponse(
                alumno.getId(),
                alumno.getCodigo(),
                alumno.getNumeroDocumento(),
                alumno.getNombres(),
                alumno.getApellidoPaterno(),
                alumno.getApellidoMaterno(),
                LocalDate.parse(alumno.getFechaNacimiento()));
    }

    @Override
    @Transactional
    @Auditable(modulo = "alumno", operacion = TipoOperacionAuditoria.DELETE)
    public void eliminar(Long id) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ALUMNO_NO_ENCONTRADO, "Alumno no encontrado"));
        alumno.setEstado(false);
        alumnoRepository.save(alumno);
    }
}