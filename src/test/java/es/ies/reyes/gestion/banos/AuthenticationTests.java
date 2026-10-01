package es.ies.reyes.gestion.banos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import es.ies.reyes.gestion.banos.models.PerfilUsuario;
import es.ies.reyes.gestion.banos.models.Profesor;
import es.ies.reyes.gestion.banos.models.UsuarioApp;
import es.ies.reyes.gestion.banos.repositories.ProfesorRepository;
import es.ies.reyes.gestion.banos.repositories.UsuarioAppRepository;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioAppRepository usuarioRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararUsuarios() {
        usuarioRepository.deleteAll();
        Profesor profesorCatalogos = obtenerOCrearProfesor(
                "00000001A", "Marta", "López", "marta@example.test");
        Profesor profesorPermisos = obtenerOCrearProfesor(
                "00000002B", "Javier", "Sánchez", "javier@example.test");
        obtenerOCrearProfesor("00000003C", "Elena", "Ruiz", "elena@example.test");
        guardarUsuario("test-password-catalogo",
                PerfilUsuario.GESTION_CATALOGOS, profesorCatalogos);
        guardarUsuario("test-password-salidas",
                PerfilUsuario.GESTION_PERMISOS, profesorPermisos);
    }

    @Test
    void requiereAutenticacionParaLaApi() throws Exception {
        mockMvc.perform(get("/api/permisos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioDeSalidasPuedeConsultarCatalogosPeroNoModificarlos() throws Exception {
        Sesion sesion = iniciarSesion("javier@example.test", "test-password-salidas");
        mockMvc.perform(autenticado(get("/api/alumnos"), sesion))
                .andExpect(status().isOk());

        mockMvc.perform(autenticado(post("/api/alumnos"), sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dni":"12345678Z","nombre":"Ana","apellidos":"Pérez","grupoId":1}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioDeSalidasPuedeCrearPermisos() throws Exception {
        Sesion sesion = iniciarSesion("javier@example.test", "test-password-salidas");
        mockMvc.perform(autenticado(post("/api/permisos"), sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usuarioDeCatalogosPuedeGestionarCatalogos() throws Exception {
        Sesion sesion = iniciarSesion("marta@example.test", "test-password-catalogo");
        mockMvc.perform(autenticado(post("/api/alumnos"), sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaCambiosSinTokenCsrf() throws Exception {
        Sesion sesion = iniciarSesion("javier@example.test", "test-password-salidas");
        mockMvc.perform(post("/api/permisos")
                        .session(sesion.session())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginRechazaUnaContrasenaIncorrecta() throws Exception {
        MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        String token = tokenCsrf(csrfResult);
        Cookie cookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");

        mockMvc.perform(post("/api/auth/login")
                        .cookie(cookie)
                        .header("X-XSRF-TOKEN", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"javier@example.test","password":"clave-incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginEstableceUnaSesionConElPerfilCorrespondiente() throws Exception {
        Sesion sesion = iniciarSesion("javier@example.test", "test-password-salidas");
        mockMvc.perform(autenticado(get("/api/auth/me"), sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("javier@example.test"));

        mockMvc.perform(autenticado(post("/api/auth/logout"), sesion))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profesorPuedeCambiarSuContrasena() throws Exception {
        Sesion sesion = iniciarSesion("javier@example.test", "test-password-salidas");
        mockMvc.perform(autenticado(put("/api/auth/password"), sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contrasenaActual":"clave-incorrecta","contrasenaNueva":"nueva-clave-segura-2026"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(autenticado(put("/api/auth/password"), sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contrasenaActual":"test-password-salidas","contrasenaNueva":"nueva-clave-segura-2026"}
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .cookie(sesion.cookie())
                        .header("X-XSRF-TOKEN", sesion.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"javier@example.test","password":"nueva-clave-segura-2026"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void gestorPuedeCrearCuentaVinculadaAProfesorYAsignarPermisoDeCatalogos() throws Exception {
        Sesion sesion = iniciarSesion("marta@example.test", "test-password-catalogo");
        Long profesorId = profesorRepository.findByDni("00000003C").orElseThrow().getId();
        mockMvc.perform(autenticado(post("/api/usuarios"), sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"clave-segura-profesor-2026","profesorId":%d,"perfil":"GESTION_CATALOGOS"}
                                """.formatted(profesorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("elena@example.test"))
                .andExpect(jsonPath("$.perfil").value("GESTION_CATALOGOS"))
                .andExpect(jsonPath("$.profesorDni").value("00000003C"));

        UsuarioApp nuevoUsuario = usuarioRepository.findByProfesorEmailIgnoreCase("elena@example.test").orElseThrow();
        Sesion nuevaSesion = iniciarSesion("elena@example.test", "clave-segura-profesor-2026");
        mockMvc.perform(autenticado(post("/api/alumnos"), nuevaSesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(autenticado(put("/api/usuarios/{id}/perfil", nuevoUsuario.getId()), sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"perfil\":\"GESTION_PERMISOS\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(autenticado(post("/api/alumnos"), nuevaSesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void impideRetirarOLiminarElUltimoGestorDeCatalogos() throws Exception {
        Sesion sesion = iniciarSesion("marta@example.test", "test-password-catalogo");
        Long usuarioId = usuarioRepository.findByProfesorEmailIgnoreCase("marta@example.test").orElseThrow().getId();
        mockMvc.perform(autenticado(put("/api/usuarios/{id}/perfil", usuarioId), sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"perfil\":\"GESTION_PERMISOS\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(autenticado(delete("/api/usuarios/{id}", usuarioId), sesion))
                .andExpect(status().isConflict());
    }

    @Test
    void elCorreoActualizadoDelProfesorPasaASerSuIdentificadorDeAcceso() throws Exception {
        Sesion sesion = iniciarSesion("marta@example.test", "test-password-catalogo");
        Long profesorId = profesorRepository.findByDni("00000002B").orElseThrow().getId();
        mockMvc.perform(autenticado(put("/api/profesores/{id}", profesorId), sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dni":"00000002B","nombre":"Javier","apellidos":"Sánchez","email":"javier.nuevo@example.test"}
                                """))
                .andExpect(status().isOk());

        iniciarSesion("javier.nuevo@example.test", "test-password-salidas");
        mockMvc.perform(post("/api/auth/login")
                        .cookie(sesion.cookie())
                        .header("X-XSRF-TOKEN", sesion.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"javier@example.test","password":"test-password-salidas"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profesorNoPuedeGestionarCuentas() throws Exception {
        Sesion sesion = iniciarSesion("javier@example.test", "test-password-salidas");
        mockMvc.perform(autenticado(get("/api/usuarios"), sesion))
                .andExpect(status().isForbidden());
    }

    private Profesor obtenerOCrearProfesor(String dni, String nombre, String apellidos, String email) {
        Profesor profesor = profesorRepository.findByDni(dni)
                .orElseGet(() -> new Profesor(dni, nombre, apellidos, email));
        if (profesor.getId() != null) {
            profesor.actualizar(dni, nombre, apellidos, email);
        }
        return profesorRepository.save(profesor);
    }

    private void guardarUsuario(String password, PerfilUsuario perfil, Profesor profesor) {
        String email = profesor.getEmail();
        var usuario = usuarioRepository.findByProfesorEmailIgnoreCase(email)
                .orElseGet(() -> new UsuarioApp(email, passwordEncoder.encode(password), perfil, profesor));
        usuario.cambiarContrasena(passwordEncoder.encode(password));
        usuario.cambiarPerfil(perfil);
        usuarioRepository.save(usuario);
    }

    private Sesion iniciarSesion(String email, String password) throws Exception {
        MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        String token = tokenCsrf(csrfResult);
        Cookie cookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        if (cookie == null) throw new AssertionError("La respuesta CSRF no incluye su cookie");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .cookie(cookie)
                        .header("X-XSRF-TOKEN", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return new Sesion((MockHttpSession) loginResult.getRequest().getSession(false), cookie, token);
    }

    private String tokenCsrf(MvcResult csrfResult) throws Exception {
        var tokenMatcher = java.util.regex.Pattern.compile("\"token\":\"([^\"]+)\"")
                .matcher(csrfResult.getResponse().getContentAsString());
        if (!tokenMatcher.find()) throw new AssertionError("La respuesta CSRF no incluye el token");
        return tokenMatcher.group(1);
    }

    private MockHttpServletRequestBuilder autenticado(
            MockHttpServletRequestBuilder request,
            Sesion sesion) {
        return request.session(sesion.session())
                .cookie(sesion.cookie())
                .header("X-XSRF-TOKEN", sesion.token());
    }

    private record Sesion(MockHttpSession session, Cookie cookie, String token) {
    }
}
