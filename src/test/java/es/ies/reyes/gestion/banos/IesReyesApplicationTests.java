package es.ies.reyes.gestion.banos;

import es.ies.reyes.gestion.banos.models.Alumno;
import es.ies.reyes.gestion.banos.models.FranjaHoraria;
import es.ies.reyes.gestion.banos.models.Grupo;
import es.ies.reyes.gestion.banos.models.Profesor;
import es.ies.reyes.gestion.banos.repositories.AlumnoRepository;
import es.ies.reyes.gestion.banos.repositories.FranjaHorariaRepository;
import es.ies.reyes.gestion.banos.repositories.GrupoRepository;
import es.ies.reyes.gestion.banos.repositories.PermisoBanoRepository;
import es.ies.reyes.gestion.banos.repositories.ProfesorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
    private GrupoRepository grupoRepository;

    @Autowired
    private FranjaHorariaRepository franjaRepository;

    private Alumno alumno;
    private Profesor profesor;
    private FranjaHoraria franja;

    @BeforeEach
    void prepararCatalogos() {
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
        franja = franjaRepository.save(new FranjaHoraria(3, "3º Hora"));
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

        mockMvc.perform(post("/api/permisos")
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
        mockMvc.perform(post("/api/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2026-09-28\",\"hora\":\"10:15:00\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.alumnoId").exists())
                .andExpect(jsonPath("$.errores.profesorId").exists());
    }
}
