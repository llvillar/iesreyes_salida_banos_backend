package es.ies.reyes.gestion.banos.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios_app")
public class UsuarioApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "contrasena_hash", nullable = false, length = 100)
    private String contrasenaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PerfilUsuario perfil;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesor_id", nullable = false, unique = true)
    private Profesor profesor;

    protected UsuarioApp() {
    }

    public UsuarioApp(String email, String contrasenaHash, PerfilUsuario perfil, Profesor profesor) {
        this.email = email;
        this.contrasenaHash = contrasenaHash;
        this.perfil = perfil;
        this.profesor = profesor;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public void cambiarContrasena(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public void cambiarPerfil(PerfilUsuario perfil) {
        this.perfil = perfil;
    }

    public void cambiarEmail(String email) {
        this.email = email;
    }
}
