package com.exemplo.representantes.web;

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

import com.exemplo.representantes.aplicacao.RepresentanteService;
import com.exemplo.representantes.dominio.Representante;
import com.exemplo.representantes.dominio.RepresentanteJaCadastradoException;
import com.exemplo.representantes.dominio.RepresentanteNaoEncontradoException;

/**
 * Teste unitário do controller isolando o framework web:
 * nenhum contexto Spring, nenhum servlet, nenhuma requisição HTTP.
 * Os métodos do controller são chamados diretamente e o serviço é um mock.
 */
@ExtendWith(MockitoExtension.class)
class RepresentanteControllerTest {
    private static final String CPF = "12345678901";

    @Mock
    private RepresentanteService service;

    @InjectMocks
    private RepresentanteController controller;

    @Test
    @DisplayName("Cadastrar devolve 201 Created com Location e corpo")
    void cadastrar_retornaCreated() {
        Representante representante = new Representante(CPF, "Ana");
        when(service.cadastrar(representante)).thenReturn(representante);

        ResponseEntity<Representante> resposta = controller.cadastrar(representante);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getHeaders().getLocation()).isEqualTo(URI.create("/representantes/" + CPF));
        assertThat(resposta.getBody()).isEqualTo(representante);
    }

    @Test
    @DisplayName("Listar sem nome delega para listarTodos")
    void listar_semNome_listaTodos() {
        List<Representante> representantes = List.of(new Representante(CPF, "Ana"));
        when(service.listarTodos()).thenReturn(representantes);

        assertThat(controller.listar(null)).isEqualTo(representantes);
        verify(service, never()).buscarPorNome(anyString());
    }

    @Test
    @DisplayName("Listar com nome delega para buscarPorNome")
    void listar_comNome_buscaPorNome() {
        List<Representante> representantes = List.of(new Representante(CPF, "Ana"));
        when(service.buscarPorNome("an")).thenReturn(representantes);

        assertThat(controller.listar("an")).isEqualTo(representantes);
        verify(service, never()).listarTodos();
    }

    @Test
    @DisplayName("Buscar por CPF devolve o representante do serviço")
    void buscarPorCpf_retornaRepresentante() {
        Representante representante = new Representante(CPF, "Ana");
        when(service.buscarPorCpf(CPF)).thenReturn(representante);

        assertThat(controller.buscarPorCpf(CPF)).isEqualTo(representante);
    }

    @Test
    @DisplayName("Buscar por CPF propaga a exceção de não encontrado")
    void buscarPorCpf_inexistente_propagaExcecao() {
        when(service.buscarPorCpf(CPF)).thenThrow(new RepresentanteNaoEncontradoException(CPF));

        assertThrows(RepresentanteNaoEncontradoException.class, () -> controller.buscarPorCpf(CPF));
    }

    @Test
    @DisplayName("Handler de exceções converte erros de domínio em status HTTP")
    void exceptionHandler_mapeiaStatus() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        assertThat(handler.naoEncontrado(new RepresentanteNaoEncontradoException(CPF)).getStatus()).isEqualTo(404);
        assertThat(handler.duplicado(new RepresentanteJaCadastradoException(CPF)).getStatus()).isEqualTo(409);
        assertThat(handler.invalido(new IllegalArgumentException("x")).getStatus()).isEqualTo(400);
    }
}
