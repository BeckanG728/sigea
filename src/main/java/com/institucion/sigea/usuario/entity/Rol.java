package com.institucion.sigea.usuario.entity;

import com.institucion.sigea.core.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rol")
@Getter
@Setter
@NoArgsConstructor
public class Rol extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_rol")
    private Long id;

    @Column(name = "nombre_rol", nullable = false, unique = true, length = 40)
    private String nombre;

    @OneToMany(mappedBy = "rol")
    private List<Usuario> usuarios = new ArrayList<>();

    public Rol(String nombre) {
        this.nombre = nombre;
    }
}
