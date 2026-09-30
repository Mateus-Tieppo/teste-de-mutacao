package com.exemplo.pecas.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.exemplo.pecas.aplicacao.PecaService;
import com.exemplo.pecas.dominio.Peca;
import com.exemplo.pecas.dominio.PecaJaCadastradaException;
import com.exemplo.pecas.dominio.PecaNaoEncontradaException;

/**
 * Teste de integração da fatia web (@WebMvcTest): sobe só o Spring MVC
 * (rotas, JSON, tratamento de erros) com o serviço mockado.
 */
@WebMvcTest(PecaController.class)
class PecaControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PecaService service;

    @Test
    @DisplayName("POST /pecas devolve 201 e o JSON da peça")
    void post_cadastra() throws Exception {
        Peca peca = new Peca(1L, "Parafuso", "M8");
        given(service.cadastrar(peca)).willReturn(peca);

        mockMvc.perform(post("/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"nome\":\"Parafuso\",\"descricao\":\"M8\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/pecas/1"))
                .andExpect(jsonPath("$.nome").value("Parafuso"));
    }

    @Test
    @DisplayName("POST /pecas com id duplicado devolve 409")
    void post_duplicada() throws Exception {
        given(service.cadastrar(new Peca(1L, "Parafuso", "M8"))).willThrow(new PecaJaCadastradaException(1L));

        mockMvc.perform(post("/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"nome\":\"Parafuso\",\"descricao\":\"M8\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /pecas inválida devolve 400")
    void post_invalida() throws Exception {
        given(service.cadastrar(new Peca(1L, "", "M8"))).willThrow(new IllegalArgumentException("Nome da peça é obrigatório"));

        mockMvc.perform(post("/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"nome\":\"\",\"descricao\":\"M8\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Nome da peça é obrigatório"));
    }

    @Test
    @DisplayName("GET /pecas lista todas")
    void get_listaTodas() throws Exception {
        given(service.listarTodas()).willReturn(List.of(new Peca(1L, "A", "a"), new Peca(2L, "B", "b")));

        mockMvc.perform(get("/pecas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /pecas?nome= consulta por nome")
    void get_porNome() throws Exception {
        given(service.buscarPorNome("para")).willReturn(List.of(new Peca(1L, "Parafuso", "M8")));

        mockMvc.perform(get("/pecas").param("nome", "para"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Parafuso"));
    }

    @Test
    @DisplayName("GET /pecas/{id} inexistente devolve 404")
    void get_porId_naoEncontrada() throws Exception {
        given(service.buscarPorId(9L)).willThrow(new PecaNaoEncontradaException(9L));

        mockMvc.perform(get("/pecas/9"))
                .andExpect(status().isNotFound());
    }
}
