package com.exemplo.clientes.persistencia;

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

import com.exemplo.clientes.dominio.Cliente;

/**
 * Teste unitário do código de persistência isolando o framework de
 * persistência e o banco: o Spring Data (ClienteJpaRepository) é um mock,
 * então nenhum EntityManager, Hibernate ou H2 é iniciado.
 * Verifica a conversão domínio <-> entidade e a delegação correta.
 */
@ExtendWith(MockitoExtension.class)
class ClienteRepositoryAdapterTest {
    private static final String CPF = "12345678901";

    @Mock
    private ClienteJpaRepository jpa;

    @InjectMocks
    private ClienteRepositoryAdapter adapter;

    @Captor
    private ArgumentCaptor<ClienteEntity> entityCaptor;

    @Test
    @DisplayName("Salvar converte o domínio em entidade e a entidade salva de volta em domínio")
    void salvar_converteEDelega() {
        when(jpa.save(any(ClienteEntity.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Cliente resultado = adapter.salvar(new Cliente(CPF, "Ana"));

        verify(jpa).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getCpf()).isEqualTo(CPF);
        assertThat(entityCaptor.getValue().getNome()).isEqualTo("Ana");
        assertThat(resultado).isEqualTo(new Cliente(CPF, "Ana"));
    }

    @Test
    @DisplayName("Buscar por CPF existente retorna o cliente convertido")
    void buscarPorCpf_existente() {
        when(jpa.findById(CPF)).thenReturn(Optional.of(new ClienteEntity(CPF, "Ana")));

        assertThat(adapter.buscarPorCpf(CPF)).contains(new Cliente(CPF, "Ana"));
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
                new ClienteEntity(CPF, "Ana"),
                new ClienteEntity("98765432100", "Anderson")));

        assertThat(adapter.buscarPorNome("an")).containsExactly(
                new Cliente(CPF, "Ana"),
                new Cliente("98765432100", "Anderson"));
    }

    @Test
    @DisplayName("Listar todos usa a consulta ordenada por nome")
    void listarTodos_converteLista() {
        when(jpa.findAllByOrderByNome()).thenReturn(List.of(new ClienteEntity(CPF, "Ana")));

        assertThat(adapter.listarTodos()).containsExactly(new Cliente(CPF, "Ana"));
    }

    @Test
    @DisplayName("Existe por CPF delega para existsById")
    void existePorCpf_delega() {
        when(jpa.existsById(CPF)).thenReturn(true);

        assertThat(adapter.existePorCpf(CPF)).isTrue();
    }
}
