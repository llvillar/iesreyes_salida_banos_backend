package es.ies.reyes.gestion.banos.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record PermisoBanoFiltro(
        LocalDate fecha,
        LocalDate desde,
        LocalDate hasta,
        String grupo,
        Long grupoId,
        Long alumnoId,
        String alumnoDni,
        Long profesorId,
        String profesorDni,
        Long franjaHorariaId,
        Integer numeroFranja,
        LocalTime horaDesde,
        LocalTime horaHasta
) {
}
