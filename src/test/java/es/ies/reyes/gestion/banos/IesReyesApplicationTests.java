package es.ies.reyes.gestion.banos;

import es.ies.reyes.gestion.banos.repositories.PermisoBanoRepository;
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
    private PermisoBanoRepository repository;

    @Test
    void contextLoads() {
    }

    @Test
    void crearPermisoYRecuperarlo() throws Exception {
        repository.deleteAll();
        String permiso = """
                {
                  "alumno": "Ana Pérez",
                  "fecha": "2026-09-28",
                  "franjaHoraria": "3ª hora",
                  "hora": "10:15:00",
                  "grupo": "2ºB",
                  "profesor": "Laura García"
                }
                """;

        mockMvc.perform(post("/api/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(permiso))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.alumno").value("Ana Pérez"))
                .andExpect(jsonPath("$.franjaHoraria").value("3ª hora"))
                .andExpect(jsonPath("$.hora").value("10:15:00"))
                .andExpect(jsonPath("$.grupo").value("2ºB"))
                .andExpect(jsonPath("$.profesor").value("Laura García"));

        mockMvc.perform(get("/api/permisos").param("grupo", "2ºb"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].alumno").value("Ana Pérez"));
    }

    @Test
    void rechazarPermisoIncompleto() throws Exception {
        mockMvc.perform(post("/api/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alumno\":\" \",\"grupo\":\"2ºB\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.alumno").exists())
                .andExpect(jsonPath("$.errores.fecha").exists());
    }
}
