package es.ies.reyes.gestion.banos;

import org.junit.jupiter.api.Test;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requiereAutenticacionParaLaApi() throws Exception {
        mockMvc.perform(get("/api/permisos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioDeSalidasPuedeConsultarCatalogosPeroNoModificarlos() throws Exception {
        Sesion sesion = iniciarSesion("test-salidas", "test-password-salidas");
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
        Sesion sesion = iniciarSesion("test-salidas", "test-password-salidas");
        mockMvc.perform(autenticado(post("/api/permisos"), sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usuarioDeCatalogosPuedeGestionarCatalogos() throws Exception {
        Sesion sesion = iniciarSesion("test-catalogo", "test-password-catalogo");
        mockMvc.perform(autenticado(post("/api/alumnos"), sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaCambiosSinTokenCsrf() throws Exception {
        Sesion sesion = iniciarSesion("test-salidas", "test-password-salidas");
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
                                {"username":"test-salidas","password":"clave-incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginEstableceUnaSesionConElPerfilCorrespondiente() throws Exception {
        Sesion sesion = iniciarSesion("test-salidas", "test-password-salidas");
        mockMvc.perform(autenticado(get("/api/auth/me"), sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test-salidas"));

        mockMvc.perform(autenticado(post("/api/auth/logout"), sesion))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    private Sesion iniciarSesion(String username, String password) throws Exception {
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
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
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
