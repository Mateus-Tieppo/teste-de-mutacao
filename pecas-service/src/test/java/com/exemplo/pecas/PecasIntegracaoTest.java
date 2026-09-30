package com.exemplo.pecas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Teste de integração completo (@SpringBootTest): controller + serviço +
 * adaptador + JPA + H2, sem mocks. Também confere a métrica no Prometheus.
 */
@SpringBootTest(properties = "management.endpoints.web.exposure.include=prometheus")
@AutoConfigureMockMvc
@AutoConfigureObservability // em testes o Boot desliga os exportadores de métricas por padrão
@DirtiesContext
class PecasIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Cadastra, consulta por id, por nome, lista e expõe a métrica")
    void fluxoCompleto() throws Exception {
        mockMvc.perform(post("/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":100,\"nome\":\"Correia\",\"descricao\":\"Correia dentada\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":100,\"nome\":\"Outra\",\"descricao\":\"x\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/pecas/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Correia dentada"));

        mockMvc.perform(get("/pecas").param("nome", "corr"))
                .andExpect(jsonPath("$[0].id").value(100));

        mockMvc.perform(get("/pecas"))
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/pecas/999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("pecas_cadastradas_total")));
    }
}
