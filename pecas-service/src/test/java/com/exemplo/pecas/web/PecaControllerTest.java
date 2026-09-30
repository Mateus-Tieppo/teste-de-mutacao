package com.exemplo.pecas.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.exemplo.pecas.aplicacao.PecaService;
import com.exemplo.pecas.dominio.Peca;
import com.exemplo.pecas.dominio.PecaNaoEncontradaException;

/**
 * Teste unitário do controller isolando o framework web:
 * nenhum contexto Spring, nenhum servlet, nenhuma requisição HTTP.
 * Os métodos do controller são chamados diretamente e o serviço é um mock.
 */
@ExtendWith(MockitoExtension.class)
class PecaControllerTest {

    @Mock
    private PecaService service;

    @InjectMocks
    private PecaController controller;

    @Test
    @DisplayName("Cadastrar devolve 201 Created com Location e corpo")
    void cadastrar_retornaCreated() {
        Peca peca = new Peca(5L, "Engrenagem", "Engrenagem 20 dentes");
        when(service.cadastrar(peca)).thenReturn(peca);

        ResponseEntity<Peca> resposta = controller.cadastrar(peca);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getHeaders().getLocation()).isEqualTo(URI.create("/pecas/5"));
        assertThat(resposta.getBody()).isEqualTo(peca);
    }

    @Test
    @DisplayName("Listar sem nome delega para listarTodas")
    void listar_semNome_listaTodas() {
        List<Peca> pecas = List.of(new Peca(1L, "A", "a"));
        when(service.listarTodas()).thenReturn(pecas);

        assertThat(controller.listar(null)).isEqualTo(pecas);
        verify(service, never()).buscarPorNome(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("Listar com nome delega para buscarPorNome")
    void listar_comNome_buscaPorNome() {
        List<Peca> pecas = List.of(new Peca(1L, "Parafuso", "M8"));
        when(service.buscarPorNome("para")).thenReturn(pecas);

        assertThat(controller.listar("para")).isEqualTo(pecas);
        verify(service, never()).listarTodas();
    }

    @Test
    @DisplayName("Buscar por id devolve a peça do serviço")
    void buscarPorId_retornaPeca() {
        Peca peca = new Peca(7L, "Mola", "Mola helicoidal");
        when(service.buscarPorId(7L)).thenReturn(peca);

        assertThat(controller.buscarPorId(7L)).isEqualTo(peca);
    }

    @Test
    @DisplayName("Buscar por id propaga a exceção de não encontrada")
    void buscarPorId_inexistente_propagaExcecao() {
        when(service.buscarPorId(7L)).thenThrow(new PecaNaoEncontradaException(7L));

        assertThrows(PecaNaoEncontradaException.class, () -> controller.buscarPorId(7L));
    }

    @Test
    @DisplayName("Handler de exceções converte erros de domínio em status HTTP")
    void exceptionHandler_mapeiaStatus() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        assertThat(handler.naoEncontrada(new PecaNaoEncontradaException(1L)).getStatus()).isEqualTo(404);
        assertThat(handler.duplicada(new com.exemplo.pecas.dominio.PecaJaCadastradaException(1L)).getStatus()).isEqualTo(409);
        assertThat(handler.invalida(new IllegalArgumentException("x")).getStatus()).isEqualTo(400);
    }
}
