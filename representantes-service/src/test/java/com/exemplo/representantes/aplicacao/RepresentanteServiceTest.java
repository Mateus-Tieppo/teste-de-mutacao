package com.exemplo.representantes.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

import com.exemplo.representantes.dominio.Representante;
import com.exemplo.representantes.dominio.RepresentanteJaCadastradoException;
import com.exemplo.representantes.dominio.RepresentanteNaoEncontradoException;
import com.exemplo.representantes.dominio.RepresentanteRepository;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Teste unitário do serviço: o repositório é um mock (Mockito),
 * sem Spring, sem JPA e sem banco de dados.
 */
@ExtendWith(MockitoExtension.class)
class RepresentanteServiceTest {
    private static final String CPF = "12345678901";

    @Mock
    private RepresentanteRepository repository;

    private SimpleMeterRegistry meterRegistry;
    private RepresentanteService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new RepresentanteService(repository, meterRegistry);
    }

    @Test
    @DisplayName("Cadastra representante normalizando CPF com máscara e incrementa a métrica")
    void cadastrar_cpfComMascara_normalizaESalva() {
        // Arrange
        Representante esperado = new Representante(CPF, "Ana Souza");
        when(repository.existePorCpf(CPF)).thenReturn(false);
        when(repository.salvar(esperado)).thenReturn(esperado);

        // Act
        Representante resultado = service.cadastrar(new Representante("123.456.789-01", "  Ana Souza "));

        // Assert
        assertThat(resultado).isEqualTo(esperado);
        verify(repository).salvar(esperado);
        assertThat(meterRegistry.counter("representantes.cadastrados").count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Não cadastra CPF já existente")
    void cadastrar_cpfDuplicado_lancaExcecao() {
        when(repository.existePorCpf(CPF)).thenReturn(true);

        assertThrows(RepresentanteJaCadastradoException.class,
                () -> service.cadastrar(new Representante(CPF, "Ana")));

        verify(repository, never()).salvar(any());
        assertThat(meterRegistry.counter("representantes.cadastrados").count()).isZero();
    }

    @Test
    @DisplayName("Rejeita representante nulo")
    void cadastrar_nulo_lancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "123", "1234567890a", "123456789012" })
    @DisplayName("Rejeita CPF inválido")
    void cadastrar_cpfInvalido_lancaExcecao(String cpf) {
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(new Representante(cpf, "Ana")));
        verify(repository, never()).existePorCpf(anyString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    @DisplayName("Rejeita nome em branco")
    void cadastrar_nomeEmBranco_lancaExcecao(String nome) {
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(new Representante(CPF, nome)));
    }

    @Test
    @DisplayName("Busca por CPF (com máscara) retorna o representante")
    void buscarPorCpf_existente_retornaRepresentante() {
        Representante representante = new Representante(CPF, "Ana");
        when(repository.buscarPorCpf(CPF)).thenReturn(Optional.of(representante));

        assertThat(service.buscarPorCpf("123.456.789-01")).isEqualTo(representante);
    }

    @Test
    @DisplayName("Busca por CPF inexistente lança exceção")
    void buscarPorCpf_inexistente_lancaExcecao() {
        when(repository.buscarPorCpf(CPF)).thenReturn(Optional.empty());

        assertThrows(RepresentanteNaoEncontradoException.class, () -> service.buscarPorCpf(CPF));
    }

    @Test
    @DisplayName("Busca por nome repassa o nome sem espaços ao repositório")
    void buscarPorNome_repassaNomeNormalizado() {
        List<Representante> representantes = List.of(new Representante(CPF, "Ana"));
        when(repository.buscarPorNome("an")).thenReturn(representantes);

        assertThat(service.buscarPorNome(" an ")).isEqualTo(representantes);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Busca por nome exige nome")
    void buscarPorNome_semNome_lancaExcecao(String nome) {
        assertThrows(IllegalArgumentException.class, () -> service.buscarPorNome(nome));
    }

    @Test
    @DisplayName("Lista todos os representantes do repositório")
    void listarTodos_retornaListaDoRepositorio() {
        List<Representante> representantes = List.of(new Representante(CPF, "Ana"), new Representante("98765432100", "Bruno"));
        when(repository.listarTodos()).thenReturn(representantes);

        assertThat(service.listarTodos()).containsExactlyElementsOf(representantes);
    }
}
