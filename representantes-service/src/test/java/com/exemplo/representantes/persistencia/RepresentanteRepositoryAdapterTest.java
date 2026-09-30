package com.exemplo.representantes.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exemplo.representantes.dominio.Representante;

/**
 * Teste unitário do código de persistência isolando o framework de
 * persistência e o banco: o Spring Data (RepresentanteJpaRepository) é um mock,
 * então nenhum EntityManager, Hibernate ou H2 é iniciado.
 * Verifica a conversão domínio <-> entidade e a delegação correta.
 */
@ExtendWith(MockitoExtension.class)
class RepresentanteRepositoryAdapterTest {
    private static final String CPF = "12345678901";

    @Mock
    private RepresentanteJpaRepository jpa;

    @InjectMocks
    private RepresentanteRepositoryAdapter adapter;

    @Captor
    private ArgumentCaptor<RepresentanteEntity> entityCaptor;

    @Test
    @DisplayName("Salvar converte o domínio em entidade e a entidade salva de volta em domínio")
    void salvar_converteEDelega() {
        when(jpa.save(any(RepresentanteEntity.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Representante resultado = adapter.salvar(new Representante(CPF, "Ana"));

        verify(jpa).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getCpf()).isEqualTo(CPF);
        assertThat(entityCaptor.getValue().getNome()).isEqualTo("Ana");
        assertThat(resultado).isEqualTo(new Representante(CPF, "Ana"));
    }

    @Test
    @DisplayName("Buscar por CPF existente retorna o representante convertido")
    void buscarPorCpf_existente() {
        when(jpa.findById(CPF)).thenReturn(Optional.of(new RepresentanteEntity(CPF, "Ana")));

        assertThat(adapter.buscarPorCpf(CPF)).contains(new Representante(CPF, "Ana"));
    }

    @Test
    @DisplayName("Buscar por CPF inexistente retorna vazio")
    void buscarPorCpf_inexistente() {
        when(jpa.findById(CPF)).thenReturn(Optional.empty());

        assertThat(adapter.buscarPorCpf(CPF)).isEmpty();
    }

    @Test
    @DisplayName("Buscar por nome usa a consulta derivada e converte todos os resultados")
    void buscarPorNome_converteLista() {
        when(jpa.findByNomeContainingIgnoreCaseOrderByNome("an")).thenReturn(List.of(
                new RepresentanteEntity(CPF, "Ana"),
                new RepresentanteEntity("98765432100", "Anderson")));

        assertThat(adapter.buscarPorNome("an")).containsExactly(
                new Representante(CPF, "Ana"),
                new Representante("98765432100", "Anderson"));
    }

    @Test
    @DisplayName("Listar todos usa a consulta ordenada por nome")
    void listarTodos_converteLista() {
        when(jpa.findAllByOrderByNome()).thenReturn(List.of(new RepresentanteEntity(CPF, "Ana")));

        assertThat(adapter.listarTodos()).containsExactly(new Representante(CPF, "Ana"));
    }

    @Test
    @DisplayName("Existe por CPF delega para existsById")
    void existePorCpf_delega() {
        when(jpa.existsById(CPF)).thenReturn(true);

        assertThat(adapter.existePorCpf(CPF)).isTrue();
    }
}
