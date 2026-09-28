package es.ies.reyes.gestion.banos.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "permisos_bano")
public class PermisoBano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesor_id", nullable = false)
    private Profesor profesor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "franja_horaria_id", nullable = false)
    private FranjaHoraria franjaHoraria;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    protected PermisoBano() {
    }

    public PermisoBano(Alumno alumno, Profesor profesor, FranjaHoraria franjaHoraria,
                       LocalDate fecha, LocalTime hora) {
        this.alumno = alumno;
        this.profesor = profesor;
        this.franjaHoraria = franjaHoraria;
        this.fecha = fecha;
        this.hora = hora;
    }

    @PrePersist
    void asignarFechaCreacion() {
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public FranjaHoraria getFranjaHoraria() {
        return franjaHoraria;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void actualizar(Alumno alumno, Profesor profesor, FranjaHoraria franjaHoraria,
                           LocalDate fecha, LocalTime hora) {
        this.alumno = alumno;
        this.profesor = profesor;
        this.franjaHoraria = franjaHoraria;
        this.fecha = fecha;
        this.hora = hora;
    }
}
