package com.institucion.sigea.aula.specification;

import com.institucion.sigea.aula.entity.Aula;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class AulaSpecification {

    private AulaSpecification() {
    }

    public static Specification<Aula> conFiltros(Long anioAcademicoId, Long nivelId, Long gradoId, Boolean estado) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (anioAcademicoId != null) {
                predicates.add(cb.equal(root.get("anioAcademico").get("id"), anioAcademicoId));
            }
            if (nivelId != null) {
                predicates.add(cb.equal(root.get("nivel").get("id"), nivelId));
            }
            if (gradoId != null) {
                predicates.add(cb.equal(root.get("grado").get("id"), gradoId));
            }
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
