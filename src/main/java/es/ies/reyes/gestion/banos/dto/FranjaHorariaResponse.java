package es.ies.reyes.gestion.banos.dto;

import es.ies.reyes.gestion.banos.models.FranjaHoraria;

public record FranjaHorariaResponse(Long id, Integer numero, String nombre) {
    public static FranjaHorariaResponse desde(FranjaHoraria franja) {
        return new FranjaHorariaResponse(franja.getId(), franja.getNumero(), franja.getNombre());
    }
}
