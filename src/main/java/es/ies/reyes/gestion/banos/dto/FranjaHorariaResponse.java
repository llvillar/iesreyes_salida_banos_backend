package es.ies.reyes.gestion.banos.dto;

import es.ies.reyes.gestion.banos.models.FranjaHoraria;

import java.time.LocalTime;

public record FranjaHorariaResponse(Long id, Integer numero, String nombre,
                                    LocalTime horaInicio, LocalTime horaFin) {
    public static FranjaHorariaResponse desde(FranjaHoraria franja) {
        return new FranjaHorariaResponse(
                franja.getId(), franja.getNumero(), franja.getNombre(),
                franja.getHoraInicio(), franja.getHoraFin()
        );
    }
}
