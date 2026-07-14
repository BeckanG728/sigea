package com.institucion.sigea.alumno.service.impl;

import com.institucion.sigea.alumno.dto.request.AlumnoRequest;
import com.institucion.sigea.alumno.dto.response.AlumnoBusquedaResponse;
import com.institucion.sigea.alumno.dto.response.AlumnoResponse;
import com.institucion.sigea.alumno.entity.Alumno;
import com.institucion.sigea.alumno.entity.TipoDocumento;
import com.institucion.sigea.alumno.mapper.AlumnoMapper;
import com.institucion.sigea.alumno.repository.AlumnoRepository;
import com.institucion.sigea.alumno.repository.TipoDocumentoRepository;
import com.institucion.sigea.alumno.service.AlumnoService;
import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlumnoServiceImpl implements AlumnoService {
    private static final String FORMATO_CODIGO_ALUMNO = "AL-%04d";

    private final AlumnoRepository alumnoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final AlumnoMapper alumnoMapper;
    private final EntityManager entityManager;

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
    public List<AlumnoBusquedaResponse> buscarPorDocumento(String numero) {
        return alumnoRepository.findByNumeroDocumentoAndEstadoTrue(numero).stream()
                .map(alumnoMapper::toBusquedaResponse)
                .toList();
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