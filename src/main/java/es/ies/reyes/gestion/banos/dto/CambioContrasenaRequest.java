package es.ies.reyes.gestion.banos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioContrasenaRequest(
        @NotBlank String contrasenaActual,
        @NotBlank @Size(min = 12, max = 72) String contrasenaNueva) {
}
