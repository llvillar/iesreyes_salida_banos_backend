package es.ies.reyes.gestion.banos.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record FranjaHorariaRequest(
        @NotNull @Min(1) @Max(6) Integer numero,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin
) {
    public String nombre() {
        return numero + "º Hora";
    }

    @AssertTrue(message = "La hora de fin debe ser posterior a la hora de inicio")
    public boolean isTramoHorarioValido() {
        return horaInicio != null && horaFin != null && horaInicio.isBefore(horaFin);
    }
}
