package es.ies.reyes.gestion.banos;

import es.ies.reyes.gestion.banos.models.Alumno;
import es.ies.reyes.gestion.banos.models.FranjaHoraria;
import es.ies.reyes.gestion.banos.models.Grupo;
import es.ies.reyes.gestion.banos.models.Profesor;
import es.ies.reyes.gestion.banos.models.PermisoBano;
import es.ies.reyes.gestion.banos.repositories.AlumnoRepository;
import es.ies.reyes.gestion.banos.repositories.FranjaHorariaRepository;
import es.ies.reyes.gestion.banos.repositories.GrupoRepository;
import es.ies.reyes.gestion.banos.repositories.PermisoBanoRepository;
import es.ies.reyes.gestion.banos.repositories.ProfesorRepository;
import es.ies.reyes.gestion.banos.repositories.UsuarioAppRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class IesReyesApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PermisoBanoRepository permisoRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private UsuarioAppRepository usuarioRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private FranjaHorariaRepository franjaRepository;

    private Alumno alumno;
    private Profesor profesor;
    private FranjaHoraria franja;

    @BeforeEach
    void prepararCatalogos() {
        usuarioRepository.deleteAll();
        permisoRepository.deleteAll();
        alumnoRepository.deleteAll();
        profesorRepository.deleteAll();
        franjaRepository.deleteAll();
        grupoRepository.deleteAll();

        Grupo grupo = grupoRepository.save(new Grupo("1º ESO", "A", "1ºA ESO"));
        alumno = alumnoRepository.save(new Alumno(
                "12345678Z", "Ana", "Pérez", "ana.perez@example.test", grupo));
        profesor = profesorRepository.save(new Profesor(
                "87654321X", "Laura", "García", "laura.garcia@example.test"));
        franja = franjaRepository.save(new FranjaHoraria(
                3, "3º Hora", LocalTime.of(10, 10), LocalTime.of(11, 0)));
    }

    @Test
    void contextLoads() {
    }

    @Test
    void crearPermisoConReferenciasNormalizadas() throws Exception {
        String permiso = """
                {
                  "alumnoId": %d,
                  "profesorId": %d,
                  "franjaHorariaId": %d,
                  "fecha": "2026-09-28",
                  "hora": "10:15:00"
                }
                """.formatted(alumno.getId(), profesor.getId(), franja.getId());

        mockMvc.perform(post("/api/permisos").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(permiso))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.alumno.dni").value("12345678Z"))
                .andExpect(jsonPath("$.alumno.grupo.codigo").value("1ºA ESO"))
                .andExpect(jsonPath("$.profesor.nombre").value("Laura"))
                .andExpect(jsonPath("$.franjaHoraria.nombre").value("3º Hora"))
                .andExpect(jsonPath("$.hora").value("10:15:00"));

        mockMvc.perform(get("/api/permisos").param("grupo", "1ºa eso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].alumno.nombre").value("Ana"));
    }

    @Test
    void rechazarPermisoSinReferencias() throws Exception {
        mockMvc.perform(post("/api/permisos").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2026-09-28\",\"hora\":\"10:15:00\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.alumnoId").exists())
                .andExpect(jsonPath("$.errores.profesorId").exists());
    }

    @Test
    void crudCompletoDeCatalogos() throws Exception {
        mockMvc.perform(get("/api/alumnos/{id}", alumno.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("12345678Z"));

        String alumnoNuevo = """
                {
                  "dni": "11223344A",
                  "nombre": "Luis",
                  "apellidos": "Serrano",
                  "email": "luis@example.test",
                  "grupoId": %d
                }
                """.formatted(alumno.getGrupo().getId());
        String alumnoActualizado = """
                {
                  "dni": "11223344A",
                  "nombre": "Luis",
                  "apellidos": "Serrano Díaz",
                  "email": "luis@example.test",
                  "grupoId": %d
                }
                """.formatted(alumno.getGrupo().getId());
        long alumnoNuevoId = idDe(mockMvc.perform(post("/api/alumnos").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alumnoNuevo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Luis"))
                .andReturn());
        mockMvc.perform(put("/api/alumnos/{id}", alumnoNuevoId).with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alumnoActualizado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apellidos").value("Serrano Díaz"));
        mockMvc.perform(delete("/api/alumnos/{id}", alumnoNuevoId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/profesores/{id}", profesor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("87654321X"));
        String profesorNuevo = """
                {"dni":"22334455B","nombre":"Pablo","apellidos":"Vega","email":"pablo@example.test"}
                """;
        String profesorActualizado = """
                {"dni":"22334455B","nombre":"Pablo","apellidos":"Vega Ruiz","email":"pablo@example.test"}
                """;
        long profesorNuevoId = idDe(mockMvc.perform(post("/api/profesores").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profesorNuevo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Pablo"))
                .andReturn());
        mockMvc.perform(put("/api/profesores/{id}", profesorNuevoId).with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profesorActualizado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apellidos").value("Vega Ruiz"));
        mockMvc.perform(delete("/api/profesores/{id}", profesorNuevoId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/grupos/{id}", alumno.getGrupo().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("1ºA ESO"));
        String grupoNuevo = """
                {"curso":"4º ESO","seccion":"A"}
                """;
        long grupoNuevoId = idDe(mockMvc.perform(post("/api/grupos").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grupoNuevo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("4ºA ESO"))
                .andReturn());
        mockMvc.perform(put("/api/grupos/{id}", grupoNuevoId).with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"curso\":\"4º ESO\",\"seccion\":\"B\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("4ºB ESO"));
        mockMvc.perform(delete("/api/grupos/{id}", grupoNuevoId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/franjas-horarias/{id}", franja.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("3º Hora"));
        long franjaNuevaId = idDe(mockMvc.perform(post("/api/franjas-horarias").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numero\":6,\"horaInicio\":\"12:40\",\"horaFin\":\"13:30\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("6º Hora"))
                .andExpect(jsonPath("$.horaInicio").value("12:40:00"))
                .andExpect(jsonPath("$.horaFin").value("13:30:00"))
                .andReturn());
        mockMvc.perform(put("/api/franjas-horarias/{id}", franjaNuevaId).with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numero\":5,\"horaInicio\":\"11:50\",\"horaFin\":\"12:40\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("5º Hora"))
                .andExpect(jsonPath("$.horaInicio").value("11:50:00"))
                .andExpect(jsonPath("$.horaFin").value("12:40:00"));
        mockMvc.perform(put("/api/franjas-horarias/{id}", franjaNuevaId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numero\":5,\"horaInicio\":\"12:40\",\"horaFin\":\"11:50\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete("/api/franjas-horarias/{id}", franjaNuevaId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void impedirEliminarGrupoConAlumnos() throws Exception {
        mockMvc.perform(delete("/api/grupos/{id}", alumno.getGrupo().getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void filtrarHistorialPorGrupoAlumnoProfesorFranjaYFechas() throws Exception {
        Grupo otroGrupo = grupoRepository.save(new Grupo("2º ESO", "B", "2ºB ESO"));
        Alumno otroAlumno = alumnoRepository.save(new Alumno(
                "22345678A", "Luis", "Serrano", "luis@example.test", otroGrupo));
        Profesor otroProfesor = profesorRepository.save(new Profesor(
                "97654321X", "Pablo", "Vega", "pablo@example.test"));
        FranjaHoraria otraFranja = franjaRepository.save(new FranjaHoraria(
                4, "4º Hora", LocalTime.of(11, 0), LocalTime.of(11, 50)));

        permisoRepository.save(new PermisoBano(
                alumno, profesor, franja, LocalDate.of(2026, 9, 28), LocalTime.of(10, 15)));
        permisoRepository.save(new PermisoBano(
                otroAlumno, otroProfesor, otraFranja, LocalDate.of(2026, 9, 29), LocalTime.of(11, 20)));

        mockMvc.perform(get("/api/permisos")
                        .param("desde", "2026-09-28")
                        .param("hasta", "2026-09-29")
                        .param("grupoId", alumno.getGrupo().getId().toString())
                        .param("alumnoDni", alumno.getDni())
                        .param("profesorId", profesor.getId().toString())
                        .param("numeroFranja", franja.getNumero().toString())
                        .param("horaDesde", "10:00:00")
                        .param("horaHasta", "10:30:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].alumno.dni").value("12345678Z"));

        mockMvc.perform(get("/api/permisos")
                        .param("fecha", "2026-09-29")
                        .param("grupo", "2ºB ESO")
                        .param("alumnoId", otroAlumno.getId().toString())
                        .param("profesorDni", otroProfesor.getDni())
                        .param("franjaHorariaId", otraFranja.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].alumno.dni").value("22345678A"));

        mockMvc.perform(get("/api/permisos")
                        .param("desde", "2026-09-30")
                        .param("hasta", "2026-09-28"))
                .andExpect(status().isBadRequest());
    }

    private long idDe(MvcResult resultado) throws Exception {
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)")
                .matcher(resultado.getResponse().getContentAsString());
        if (!matcher.find()) {
            throw new AssertionError("La respuesta no contiene un id");
        }
        return Long.parseLong(matcher.group(1));
    }
}
