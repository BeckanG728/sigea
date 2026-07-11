package com.institucion.sigea.matricula.service.impl;

import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.aula.entity.Aula;
import com.institucion.sigea.aula.repository.AulaRepository;
import com.institucion.sigea.auth.service.TotpService;
import com.institucion.sigea.concepto.entity.Concepto;
import com.institucion.sigea.concepto.repository.ConceptoRepository;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.matricula.dto.request.MatriculaRequest;
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
import com.institucion.sigea.security.jwt.JwtPrincipal;
import com.institucion.sigea.usuario.entity.Usuario;
import com.institucion.sigea.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final MatriculaMapper matriculaMapper;

    private final UsuarioRepository usuarioRepository;
    private final TotpService totpService;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    @Auditable(modulo = "matricula", operacion = TipoOperacionAuditoria.MATRICULA)
    public MatriculaResponse matricular(MatriculaRequest request) {
        JwtPrincipal principal = (JwtPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Usuario usuario = usuarioRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_CREDENTIALS, "Usuario no encontrado"));

        Aula aula = aulaRepository.findById(request.codAula())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.AULA_NO_ENCONTRADA, "Aula no encontrada",
                        Map.of("codAula", request.codAula())));

        matriculaValidator.validar(aula, request.codAlumno(), request.codAnioAcademico());

        if (usuario.isTotpVerificado() && usuario.isDosFactorHabilitado()) {
            String codigoOtp = request.codigoOTP();
            if (codigoOtp == null || codigoOtp.isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_OTP,
                        "Código OTP obligatorio para matricularse.");
            }
            validarOtpYRateLimit(usuario, codigoOtp);
        }

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

        MatriculaResponse response = matriculaMapper.toResponse(matricula, cuotas);

        if (!usuario.isTotpVerificado()) {
            String qrUri = totpService.generarQrUri(
                    usuario.getTotpSecret(), usuario.getNombreUsuario());
            return MatriculaResponse.withQrSetup(response, qrUri);
        }

        return response;
    }

    private void validarOtpYRateLimit(Usuario usuario, String codigoOtp) {
        Cache cache = cacheManager.getCache(
                com.institucion.sigea.config.CacheConfig.CACHE_OTP_INTENTOS_MATRICULA);
        Long userId = usuario.getId();
        String cacheKey = "intentos:" + userId;

        Integer intentos = cache.get(cacheKey, Integer.class);
        if (intentos != null && intentos >= 3) {
            throw new BusinessException(ErrorCode.OTP_BLOCKED,
                    "Demasiados intentos fallidos de OTP. Espere 5 minutos.");
        }

        try {
            totpService.verificarCodigo(usuario.getTotpSecret(), codigoOtp);
            if (intentos != null) {
                cache.evict(cacheKey);
            }
        } catch (Exception e) {
            int nuevoIntento = (intentos != null ? intentos : 0) + 1;
            cache.put(cacheKey, nuevoIntento);
            throw new BusinessException(ErrorCode.INVALID_OTP,
                    "Código OTP inválido. Intento " + nuevoIntento + " de 3.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaReporteResponse> reportar(Integer anioAcademico, Long codNivel, Long codGrado, Integer codAula) {
        return matriculaMapper.toReporteResponseList(
                matriculaRepository.buscarParaReporte(anioAcademico, codNivel, codGrado, codAula));
    }

}
