package es.ies.reyes.gestion.banos.dto;

import es.ies.reyes.gestion.banos.models.Profesor;

public record ProfesorResponse(Long id, String dni, String nombre, String apellidos, String email) {
    public static ProfesorResponse desde(Profesor profesor) {
        return new ProfesorResponse(
                profesor.getId(),
                profesor.getDni(),
                profesor.getNombre(),
                profesor.getApellidos(),
                profesor.getEmail()
        );
    }
}
