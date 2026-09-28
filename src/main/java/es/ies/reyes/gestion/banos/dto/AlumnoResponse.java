package es.ies.reyes.gestion.banos.dto;

import es.ies.reyes.gestion.banos.models.Alumno;

public record AlumnoResponse(
        Long id,
        String dni,
        String nombre,
        String apellidos,
        String email,
        GrupoResponse grupo
) {
    public static AlumnoResponse desde(Alumno alumno) {
        return new AlumnoResponse(
                alumno.getId(),
                alumno.getDni(),
                alumno.getNombre(),
                alumno.getApellidos(),
                alumno.getEmail(),
                GrupoResponse.desde(alumno.getGrupo())
        );
    }
}
