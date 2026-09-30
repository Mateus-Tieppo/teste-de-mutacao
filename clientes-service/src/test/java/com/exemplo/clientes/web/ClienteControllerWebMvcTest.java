package com.exemplo.clientes.web;

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

import com.exemplo.clientes.aplicacao.ClienteService;
import com.exemplo.clientes.dominio.Cliente;
import com.exemplo.clientes.dominio.ClienteJaCadastradoException;
import com.exemplo.clientes.dominio.ClienteNaoEncontradoException;

/**
 * Teste de integração da fatia web (@WebMvcTest): sobe só o Spring MVC
 * (rotas, JSON, tratamento de erros) com o serviço mockado.
 */
@WebMvcTest(ClienteController.class)
class ClienteControllerWebMvcTest {
    private static final String CPF = "12345678901";
    private static final String JSON = "{\"cpf\":\"12345678901\",\"nome\":\"Ana\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    @Test
    @DisplayName("POST /clientes devolve 201 e o JSON do cliente")
    void post_cadastra() throws Exception {
        Cliente cliente = new Cliente(CPF, "Ana");
        given(service.cadastrar(cliente)).willReturn(cliente);

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/clientes/" + CPF))
                .andExpect(jsonPath("$.nome").value("Ana"));
    }

    @Test
    @DisplayName("POST /clientes com CPF duplicado devolve 409")
    void post_duplicado() throws Exception {
        given(service.cadastrar(new Cliente(CPF, "Ana"))).willThrow(new ClienteJaCadastradoException(CPF));

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /clientes inválido devolve 400")
    void post_invalido() throws Exception {
        given(service.cadastrar(new Cliente("1", "Ana"))).willThrow(new IllegalArgumentException("CPF deve conter 11 dígitos"));

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"1\",\"nome\":\"Ana\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("CPF deve conter 11 dígitos"));
    }

    @Test
    @DisplayName("GET /clientes lista todos")
    void get_listaTodos() throws Exception {
        given(service.listarTodos()).willReturn(List.of(new Cliente(CPF, "Ana"), new Cliente("98765432100", "Bruno")));

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /clientes?nome= consulta por nome")
    void get_porNome() throws Exception {
        given(service.buscarPorNome("an")).willReturn(List.of(new Cliente(CPF, "Ana")));

        mockMvc.perform(get("/clientes").param("nome", "an"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cpf").value(CPF));
    }

    @Test
    @DisplayName("GET /clientes/{cpf} inexistente devolve 404")
    void get_porCpf_naoEncontrado() throws Exception {
        given(service.buscarPorCpf(CPF)).willThrow(new ClienteNaoEncontradoException(CPF));

        mockMvc.perform(get("/clientes/" + CPF))
                .andExpect(status().isNotFound());
    }
}
