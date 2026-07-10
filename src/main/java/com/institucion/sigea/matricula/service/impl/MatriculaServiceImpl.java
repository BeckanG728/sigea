package com.institucion.sigea.matricula.service.impl;

import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.aula.entity.Aula;
import com.institucion.sigea.aula.repository.AulaRepository;
import com.institucion.sigea.concepto.entity.Concepto;
import com.institucion.sigea.concepto.repository.ConceptoRepository;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.matricula.dto.request.MatriculaRequest;
import com.institucion.sigea.matricula.dto.response.CuotaResponse;
import com.institucion.sigea.matricula.dto.response.MatriculaReporteResponse;
import com.institucion.sigea.matricula.dto.response.MatriculaResponse;
import com.institucion.sigea.matricula.entity.Cuota;
import com.institucion.sigea.matricula.entity.EstadoCuota;
import com.institucion.sigea.matricula.entity.Matricula;
import com.institucion.sigea.matricula.mapper.MatriculaMapper;
import com.institucion.sigea.matricula.repository.CuotaRepository;
import com.institucion.sigea.matricula.repository.MatriculaRepository;
import com.institucion.sigea.matricula.service.MatriculaService;
import com.institucion.sigea.matricula.service.MatriculaValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import java.time.Year;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MatriculaServiceImpl implements MatriculaService {
    private static final String FORMATO_CODIGO_MATRICULA = "MAT-%d-%04d";

    private final MatriculaValidator matriculaValidator;
    private final AulaRepository aulaRepository;
    private final ConceptoRepository conceptoRepository;
    private final MatriculaRepository matriculaRepository;
    private final CuotaRepository cuotaRepository;
    private final EntityManager entityManager;

    private final MatriculaMapper matriculaMapper; // agregar al constructor (Lombok @RequiredArgsConstructor lo toma solo)

    @Override
    @Transactional
    @Auditable(modulo = "matricula", operacion = TipoOperacionAuditoria.MATRICULA)
    public MatriculaResponse matricular(MatriculaRequest request) {

        Aula aula = aulaRepository.findWithLockById(request.codAula())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.AULA_NO_ENCONTRADA, "Aula no encontrada",
                        Map.of("codAula", request.codAula())));

        matriculaValidator.validar(aula, request.codAlumno(), request.codAnioAcademico());

        Matricula matricula = new Matricula();
        matricula.setCodAlumno(request.codAlumno().intValue());
        matricula.setCodAula(request.codAula().intValue());
        matricula.setCodAnioAcademico(request.codAnioAcademico().intValue());
        matricula.setFechaMatricula(LocalDateTime.now());
        int anioActual = Year.now().getValue();
        Long siguienteCorrelativo = ((Number) entityManager
                .createNativeQuery("SELECT nextval('seq_codigo_matricula')")
                .getSingleResult()).longValue();
        matricula.setCodigo(FORMATO_CODIGO_MATRICULA.formatted(anioActual, siguienteCorrelativo));
        matriculaRepository.save(matricula);

        List<Concepto> conceptosActivos = conceptoRepository
                .findByAnioAcademicoId(request.codAnioAcademico()).stream()
                .filter(Concepto::isEstado)
                .toList();

        List<Cuota> cuotas = conceptosActivos.stream()
                .map(concepto -> {
                    Cuota cuota = new Cuota();
                    cuota.setCodMatricula(matricula.getId().intValue());
                    cuota.setCodConcepto(concepto.getId().intValue());
                    cuota.setMontoPagar(concepto.getMonto());
                    cuota.setOrdenPago(concepto.getOrdenPago());
                    cuota.setEstadoCuota(EstadoCuota.PENDIENTE);
                    return cuota;
                })
                .toList();
        cuotaRepository.saveAll(cuotas);

        return matriculaMapper.toResponse(matricula, cuotas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaReporteResponse> reportar(Integer anioAcademico, Long codNivel, Long codGrado, Integer codAula) {
        return matriculaMapper.toReporteResponseList(
                matriculaRepository.buscarParaReporte(anioAcademico, codNivel, codGrado, codAula));
    }

}
