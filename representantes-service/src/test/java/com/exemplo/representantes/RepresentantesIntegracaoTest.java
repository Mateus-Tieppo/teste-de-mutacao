package com.exemplo.representantes;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class RepresentantesIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Cadastra, consulta por CPF, por nome, lista e expõe a métrica")
    void fluxoCompleto() throws Exception {
        mockMvc.perform(post("/representantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"123.456.789-01\",\"nome\":\"Ana Souza\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("12345678901"));

        mockMvc.perform(post("/representantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"12345678901\",\"nome\":\"Outra\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/representantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"123\",\"nome\":\"Inválido\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/representantes/12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza"));

        mockMvc.perform(get("/representantes").param("nome", "souza"))
                .andExpect(jsonPath("$[0].cpf").value("12345678901"));

        mockMvc.perform(get("/representantes"))
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/representantes/99999999999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("representantes_cadastrados_total")));
    }
}
