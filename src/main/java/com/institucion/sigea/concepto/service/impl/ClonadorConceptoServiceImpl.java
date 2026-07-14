package com.institucion.sigea.concepto.service.impl;

import com.institucion.sigea.aula.repository.AnioAcademicoRepository;
import com.institucion.sigea.concepto.dto.response.ClonadoResponse;
import com.institucion.sigea.concepto.entity.Concepto;
import com.institucion.sigea.concepto.repository.ConceptoRepository;
import com.institucion.sigea.concepto.service.ClonadorConceptoService;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClonadorConceptoServiceImpl implements ClonadorConceptoService {

    private final ConceptoRepository conceptoRepository;
    private final AnioAcademicoRepository anioAcademicoRepository;

    @Override
    @Transactional
    public ClonadoResponse clonar(Long anioOrigen, Long anioDestino) {
        if (anioOrigen.equals(anioDestino)) {
            throw new BusinessException(ErrorCode.CLONADO_ANIOS_IGUALES,
                    "El año de origen y destino no pueden ser el mismo",
                    Map.of("anioOrigen", anioOrigen, "anioDestino", anioDestino));
        }

        List<Concepto> existentesDestino = conceptoRepository.findByAnioAcademicoId(anioDestino);
        if (!existentesDestino.isEmpty()) {
            return new ClonadoResponse(0, anioDestino); // idempotente: ya se clonó antes, no duplica
        }

        List<Concepto> origen = conceptoRepository.findByAnioAcademicoId(anioOrigen);
        List<Concepto> clonados = origen.stream().map(c -> {
            Concepto nuevo = new Concepto();
            nuevo.setAnioAcademico(anioAcademicoRepository.getReferenceById(anioDestino));
            nuevo.setTipoConcepto(c.getTipoConcepto());
            nuevo.setNombreConcepto(c.getNombreConcepto());
            nuevo.setMonto(c.getMonto());
            nuevo.setOrdenPago(c.getOrdenPago());
            nuevo.setObligatorio(c.isObligatorio());
            nuevo.setTipo(c.getTipo());
            return nuevo;
        }).toList();

        conceptoRepository.saveAll(clonados);
        return new ClonadoResponse(clonados.size(), anioDestino);
    }
}