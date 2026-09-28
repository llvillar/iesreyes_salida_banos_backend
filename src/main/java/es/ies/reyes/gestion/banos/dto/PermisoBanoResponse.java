package es.ies.reyes.gestion.banos.dto;

import es.ies.reyes.gestion.banos.models.PermisoBano;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record PermisoBanoResponse(
        Long id,
        LocalDate fecha,
        LocalTime hora,
        AlumnoResponse alumno,
        ProfesorResponse profesor,
        FranjaHorariaResponse franjaHoraria,
        LocalDateTime creadoEn
) {
    public static PermisoBanoResponse desde(PermisoBano permiso) {
        return new PermisoBanoResponse(
                permiso.getId(),
                permiso.getFecha(),
                permiso.getHora(),
                AlumnoResponse.desde(permiso.getAlumno()),
                ProfesorResponse.desde(permiso.getProfesor()),
                FranjaHorariaResponse.desde(permiso.getFranjaHoraria()),
                permiso.getCreadoEn()
        );
    }
}
