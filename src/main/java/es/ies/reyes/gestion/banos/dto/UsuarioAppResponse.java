package es.ies.reyes.gestion.banos.dto;

import es.ies.reyes.gestion.banos.models.PerfilUsuario;

public record UsuarioAppResponse(
        Long id,
        String email,
        PerfilUsuario perfil,
        Long profesorId,
        String profesorDni,
        String profesorNombre,
        String profesorApellidos) {
}
