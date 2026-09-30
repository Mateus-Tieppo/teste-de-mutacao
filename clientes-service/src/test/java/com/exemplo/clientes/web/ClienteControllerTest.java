package com.exemplo.clientes.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.exemplo.clientes.aplicacao.ClienteService;
import com.exemplo.clientes.dominio.Cliente;
import com.exemplo.clientes.dominio.ClienteJaCadastradoException;
import com.exemplo.clientes.dominio.ClienteNaoEncontradoException;

/**
 * Teste unitário do controller isolando o framework web:
 * nenhum contexto Spring, nenhum servlet, nenhuma requisição HTTP.
 * Os métodos do controller são chamados diretamente e o serviço é um mock.
 */
@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {
    private static final String CPF = "12345678901";

    @Mock
    private ClienteService service;

    @InjectMocks
    private ClienteController controller;

    @Test
    @DisplayName("Cadastrar devolve 201 Created com Location e corpo")
    void cadastrar_retornaCreated() {
        Cliente cliente = new Cliente(CPF, "Ana");
        when(service.cadastrar(cliente)).thenReturn(cliente);

        ResponseEntity<Cliente> resposta = controller.cadastrar(cliente);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getHeaders().getLocation()).isEqualTo(URI.create("/clientes/" + CPF));
        assertThat(resposta.getBody()).isEqualTo(cliente);
    }

    @Test
    @DisplayName("Listar sem nome delega para listarTodos")
    void listar_semNome_listaTodos() {
        List<Cliente> clientes = List.of(new Cliente(CPF, "Ana"));
        when(service.listarTodos()).thenReturn(clientes);

        assertThat(controller.listar(null)).isEqualTo(clientes);
        verify(service, never()).buscarPorNome(anyString());
    }

    @Test
    @DisplayName("Listar com nome delega para buscarPorNome")
    void listar_comNome_buscaPorNome() {
        List<Cliente> clientes = List.of(new Cliente(CPF, "Ana"));
        when(service.buscarPorNome("an")).thenReturn(clientes);

        assertThat(controller.listar("an")).isEqualTo(clientes);
        verify(service, never()).listarTodos();
    }

    @Test
    @DisplayName("Buscar por CPF devolve o cliente do serviço")
    void buscarPorCpf_retornaCliente() {
        Cliente cliente = new Cliente(CPF, "Ana");
        when(service.buscarPorCpf(CPF)).thenReturn(cliente);

        assertThat(controller.buscarPorCpf(CPF)).isEqualTo(cliente);
    }

    @Test
    @DisplayName("Buscar por CPF propaga a exceção de não encontrado")
    void buscarPorCpf_inexistente_propagaExcecao() {
        when(service.buscarPorCpf(CPF)).thenThrow(new ClienteNaoEncontradoException(CPF));

        assertThrows(ClienteNaoEncontradoException.class, () -> controller.buscarPorCpf(CPF));
    }

    @Test
    @DisplayName("Handler de exceções converte erros de domínio em status HTTP")
    void exceptionHandler_mapeiaStatus() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        assertThat(handler.naoEncontrado(new ClienteNaoEncontradoException(CPF)).getStatus()).isEqualTo(404);
        assertThat(handler.duplicado(new ClienteJaCadastradoException(CPF)).getStatus()).isEqualTo(409);
        assertThat(handler.invalido(new IllegalArgumentException("x")).getStatus()).isEqualTo(400);
    }
}
