package es.ies.reyes.gestion.banos.dto;

import es.ies.reyes.gestion.banos.models.PerfilUsuario;
import jakarta.validation.constraints.NotNull;

public record PerfilRequest(@NotNull PerfilUsuario perfil) {
}
