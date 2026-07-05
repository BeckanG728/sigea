package com.institucion.sigea.usuario.entity;

import com.institucion.sigea.core.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rol_funcionalidad", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "rol_id", "funcionalidad_id" })
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RolFuncionalidad extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionalidad_id", nullable = false)
    private Funcionalidad funcionalidad;

    @Column(nullable = false)
    private boolean ver;

    @Column(nullable = false)
    private boolean crear;

    @Column(nullable = false)
    private boolean editar;

    @Column(nullable = false)
    private boolean eliminar;

    @Column(nullable = false)
    private boolean imprimir;

    public RolFuncionalidad(Rol rol, Funcionalidad funcionalidad,
                            boolean ver, boolean crear, boolean editar,
                            boolean eliminar, boolean imprimir) {
        this.rol = rol;
        this.funcionalidad = funcionalidad;
        this.ver = ver;
        this.crear = crear;
        this.editar = editar;
        this.eliminar = eliminar;
        this.imprimir = imprimir;
        this.setEstado(true);
    }
}
