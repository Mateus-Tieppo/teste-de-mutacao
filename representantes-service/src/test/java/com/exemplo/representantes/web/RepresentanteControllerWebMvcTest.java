package com.exemplo.representantes.web;

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

import com.exemplo.representantes.aplicacao.RepresentanteService;
import com.exemplo.representantes.dominio.Representante;
import com.exemplo.representantes.dominio.RepresentanteJaCadastradoException;
import com.exemplo.representantes.dominio.RepresentanteNaoEncontradoException;

/**
 * Teste de integração da fatia web (@WebMvcTest): sobe só o Spring MVC
 * (rotas, JSON, tratamento de erros) com o serviço mockado.
 */
@WebMvcTest(RepresentanteController.class)
class RepresentanteControllerWebMvcTest {
    private static final String CPF = "12345678901";
    private static final String JSON = "{\"cpf\":\"12345678901\",\"nome\":\"Ana\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepresentanteService service;

    @Test
    @DisplayName("POST /representantes devolve 201 e o JSON do representante")
    void post_cadastra() throws Exception {
        Representante representante = new Representante(CPF, "Ana");
        given(service.cadastrar(representante)).willReturn(representante);

        mockMvc.perform(post("/representantes").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/representantes/" + CPF))
                .andExpect(jsonPath("$.nome").value("Ana"));
    }

    @Test
    @DisplayName("POST /representantes com CPF duplicado devolve 409")
    void post_duplicado() throws Exception {
        given(service.cadastrar(new Representante(CPF, "Ana"))).willThrow(new RepresentanteJaCadastradoException(CPF));

        mockMvc.perform(post("/representantes").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /representantes inválido devolve 400")
    void post_invalido() throws Exception {
        given(service.cadastrar(new Representante("1", "Ana"))).willThrow(new IllegalArgumentException("CPF deve conter 11 dígitos"));

        mockMvc.perform(post("/representantes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"1\",\"nome\":\"Ana\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("CPF deve conter 11 dígitos"));
    }

    @Test
    @DisplayName("GET /representantes lista todos")
    void get_listaTodos() throws Exception {
        given(service.listarTodos()).willReturn(List.of(new Representante(CPF, "Ana"), new Representante("98765432100", "Bruno")));

        mockMvc.perform(get("/representantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /representantes?nome= consulta por nome")
    void get_porNome() throws Exception {
        given(service.buscarPorNome("an")).willReturn(List.of(new Representante(CPF, "Ana")));

        mockMvc.perform(get("/representantes").param("nome", "an"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cpf").value(CPF));
    }

    @Test
    @DisplayName("GET /representantes/{cpf} inexistente devolve 404")
    void get_porCpf_naoEncontrado() throws Exception {
        given(service.buscarPorCpf(CPF)).willThrow(new RepresentanteNaoEncontradoException(CPF));

        mockMvc.perform(get("/representantes/" + CPF))
                .andExpect(status().isNotFound());
    }
}
