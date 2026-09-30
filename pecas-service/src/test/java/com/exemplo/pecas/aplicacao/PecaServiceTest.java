package com.exemplo.pecas.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exemplo.pecas.dominio.Peca;
import com.exemplo.pecas.dominio.PecaJaCadastradaException;
import com.exemplo.pecas.dominio.PecaNaoEncontradaException;
import com.exemplo.pecas.dominio.PecaRepository;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Teste unitário do serviço: o repositório é um mock (Mockito),
 * sem Spring, sem JPA e sem banco de dados.
 */
@ExtendWith(MockitoExtension.class)
class PecaServiceTest {

    @Mock
    private PecaRepository repository;

    private SimpleMeterRegistry meterRegistry;
    private PecaService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new PecaService(repository, meterRegistry);
    }

    @Test
    @DisplayName("Cadastra peça válida, salva no repositório e incrementa a métrica")
    void cadastrar_pecaValida_salvaEIncrementaMetrica() {
        // Arrange
        Peca peca = new Peca(10L, "  Parafuso ", " Parafuso sextavado M8 ");
        Peca esperada = new Peca(10L, "Parafuso", "Parafuso sextavado M8");
        when(repository.existePorId(10L)).thenReturn(false);
        when(repository.salvar(esperada)).thenReturn(esperada);

        // Act
        Peca resultado = service.cadastrar(peca);

        // Assert
        assertThat(resultado).isEqualTo(esperada);
        verify(repository).salvar(esperada);
        assertThat(meterRegistry.counter("pecas.cadastradas").count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Não cadastra peça com id já existente")
    void cadastrar_idDuplicado_lancaExcecao() {
        when(repository.existePorId(10L)).thenReturn(true);

        assertThrows(PecaJaCadastradaException.class,
                () -> service.cadastrar(new Peca(10L, "Parafuso", "M8")));

        verify(repository, never()).salvar(any());
        assertThat(meterRegistry.counter("pecas.cadastradas").count()).isZero();
    }

    @Test
    @DisplayName("Rejeita peça nula")
    void cadastrar_pecaNula_lancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(null));
    }

    @ParameterizedTest
    @ValueSource(longs = { 0L, -1L })
    @DisplayName("Rejeita número de identificação não positivo")
    void cadastrar_idInvalido_lancaExcecao(long id) {
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(new Peca(id, "Parafuso", "M8")));
        verify(repository, never()).salvar(any());
    }

    @Test
    @DisplayName("Rejeita id nulo")
    void cadastrar_idNulo_lancaExcecao() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(new Peca(null, "Parafuso", "M8")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    @DisplayName("Rejeita nome em branco")
    void cadastrar_nomeEmBranco_lancaExcecao(String nome) {
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(new Peca(1L, nome, "M8")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    @DisplayName("Rejeita descrição em branco")
    void cadastrar_descricaoEmBranco_lancaExcecao(String descricao) {
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(new Peca(1L, "Parafuso", descricao)));
    }

    @Test
    @DisplayName("Busca por id retorna a peça quando existe")
    void buscarPorId_existente_retornaPeca() {
        Peca peca = new Peca(1L, "Porca", "Porca M8");
        when(repository.buscarPorId(1L)).thenReturn(Optional.of(peca));

        assertThat(service.buscarPorId(1L)).isEqualTo(peca);
    }

    @Test
    @DisplayName("Busca por id lança exceção quando a peça não existe")
    void buscarPorId_inexistente_lancaExcecao() {
        when(repository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(PecaNaoEncontradaException.class, () -> service.buscarPorId(99L));
    }

    @Test
    @DisplayName("Busca por nome repassa o nome sem espaços ao repositório")
    void buscarPorNome_repassaNomeNormalizado() {
        List<Peca> pecas = List.of(new Peca(1L, "Parafuso", "M8"));
        when(repository.buscarPorNome("para")).thenReturn(pecas);

        assertThat(service.buscarPorNome("  para ")).isEqualTo(pecas);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Busca por nome exige nome")
    void buscarPorNome_semNome_lancaExcecao(String nome) {
        assertThrows(IllegalArgumentException.class, () -> service.buscarPorNome(nome));
    }

    @Test
    @DisplayName("Lista todas as peças do repositório")
    void listarTodas_retornaListaDoRepositorio() {
        List<Peca> pecas = List.of(new Peca(1L, "A", "a"), new Peca(2L, "B", "b"));
        when(repository.listarTodas()).thenReturn(pecas);

        assertThat(service.listarTodas()).containsExactlyElementsOf(pecas);
    }
}
