package com.institucion.sigea.alumno.service.impl;

import com.institucion.sigea.alumno.entity.TipoDocumento;
import com.institucion.sigea.alumno.repository.TipoDocumentoRepository;
import com.institucion.sigea.alumno.service.TipoDocumentoService;
import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoDocumentoServiceImpl implements TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    @Override
    public List<TipoDocumento> listar() {
        return tipoDocumentoRepository.findAll();
    }

    @Override
    @Transactional
    @Auditable(modulo = "tipo_documento", operacion = TipoOperacionAuditoria.DELETE)
    public void eliminar(Long id) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIPO_DOCUMENTO_NO_ENCONTRADO, "Tipo de documento no encontrado"));
        try {
            tipoDocumentoRepository.delete(tipoDocumento);
            tipoDocumentoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            tipoDocumento.setEstado(false);
            tipoDocumentoRepository.save(tipoDocumento);
        }
    }
}
