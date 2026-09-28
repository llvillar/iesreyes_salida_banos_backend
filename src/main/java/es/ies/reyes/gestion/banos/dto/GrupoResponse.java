package es.ies.reyes.gestion.banos.dto;

import es.ies.reyes.gestion.banos.models.Grupo;

public record GrupoResponse(Long id, String curso, String seccion, String codigo) {
    public static GrupoResponse desde(Grupo grupo) {
        return new GrupoResponse(grupo.getId(), grupo.getCurso(), grupo.getSeccion(), grupo.getCodigo());
    }
}
