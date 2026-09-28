package es.ies.reyes.gestion.banos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FranjaHorariaRequest(
        @NotNull @Min(1) @Max(6) Integer numero
) {
    public String nombre() {
        return numero + "º Hora";
    }
}
