package es.ies.reyes.gestion.banos.dto;

import es.ies.reyes.gestion.banos.models.PerfilUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioAppRequest(
        @NotBlank @Size(min = 12, max = 72) String password,
        @NotNull Long profesorId,
        @NotNull PerfilUsuario perfil) {
}
