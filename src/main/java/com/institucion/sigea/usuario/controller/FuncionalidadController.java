package com.institucion.sigea.usuario.controller;

import com.institucion.sigea.security.jwt.JwtPrincipal;
import com.institucion.sigea.usuario.dto.response.FuncionalidadTreeResponse;
import com.institucion.sigea.usuario.dto.response.MisPermisosResponse;
import com.institucion.sigea.usuario.dto.response.MisPermisosWrapper;
import com.institucion.sigea.usuario.entity.Rol;
import com.institucion.sigea.usuario.repository.RolRepository;
import com.institucion.sigea.usuario.service.FuncionalidadService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("funcionalidades")
@RequiredArgsConstructor
public class FuncionalidadController {

    private static final Logger log = LoggerFactory.getLogger(FuncionalidadController.class);

    private final FuncionalidadService funcionalidadService;
    private final RolRepository rolRepository;

    @GetMapping("/tree")
    @PreAuthorize("hasRole('SUPERUSUARIO')")
    public ResponseEntity<List<FuncionalidadTreeResponse>> obtenerArbol() {
        return ResponseEntity.ok(funcionalidadService.obtenerArbol());
    }

    @GetMapping
    public ResponseEntity<MisPermisosWrapper> misPermisos(Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        Long idRol = rolRepository.findByNombre(principal.rol()).map(Rol::getId).orElseThrow();
        log.info("GET /funcionalidades -> rol='{}' idRol={}", principal.rol(), idRol);

        List<MisPermisosResponse> arbol = funcionalidadService.obtenerMisPermisos(idRol);
        log.info("GET /funcionalidades -> nodos raiz devueltos: {}", arbol.size());
        for (MisPermisosResponse nodo : arbol) {
            log.info("  raiz codigo={} nombre={} ver={} hijos={}",
                    nodo.codigo(), nodo.nombre(), nodo.permisos().ver(), nodo.hijos().size());
            for (MisPermisosResponse hijo : nodo.hijos()) {
                log.info("    hijo codigo={} nombre={} ver={}",
                        hijo.codigo(), hijo.nombre(), hijo.permisos().ver());
            }
        }

        return ResponseEntity.ok(new MisPermisosWrapper(arbol));
    }
}
