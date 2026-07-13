package com.institucion.sigea.usuario.mapper;

import com.institucion.sigea.usuario.dto.response.UsuarioResponse;
import com.institucion.sigea.usuario.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "idUsuario", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "usuario", source = "nombreUsuario")
    @Mapping(target = "idRol", source = "rol.id")
    @Mapping(target = "nombreRol", source = "rol.nombre")
    UsuarioResponse toResponse(Usuario usuario);
}
