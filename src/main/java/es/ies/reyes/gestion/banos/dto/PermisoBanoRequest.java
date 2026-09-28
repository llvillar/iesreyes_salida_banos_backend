package es.ies.reyes.gestion.banos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

public record PermisoBanoRequest(
        @NotNull @Positive Long alumnoId,
        @NotNull @Positive Long profesorId,
        @NotNull @Positive Long franjaHorariaId,
        @NotNull LocalDate fecha,
        @NotNull LocalTime hora
) {
}
