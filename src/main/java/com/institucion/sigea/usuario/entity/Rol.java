package com.institucion.sigea.usuario.entity;

import com.institucion.sigea.core.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Rol extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String nombreRol;

    @OneToMany(mappedBy = "rol")
    private List<Usuario> usuarios = new ArrayList<>();

    public Rol(String nombreRol) {
        this.nombreRol = nombreRol;
    }
}
