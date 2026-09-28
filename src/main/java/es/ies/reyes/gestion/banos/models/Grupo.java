package es.ies.reyes.gestion.banos.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "grupos", uniqueConstraints = @UniqueConstraint(
        name = "uk_grupos_curso_seccion",
        columnNames = {"curso", "seccion"}
))
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String curso;

    @Column(nullable = false, length = 5)
    private String seccion;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    protected Grupo() {
    }

    public Grupo(String curso, String seccion, String codigo) {
        this.curso = curso;
        this.seccion = seccion;
        this.codigo = codigo;
    }

    public Long getId() {
        return id;
    }

    public String getCurso() {
        return curso;
    }

    public String getSeccion() {
        return seccion;
    }

    public String getCodigo() {
        return codigo;
    }
}
