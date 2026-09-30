package com.exemplo.clientes.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.exemplo.clientes.dominio.Cliente;

/**
 * Teste de integração da fatia de persistência (@DataJpaTest): JPA real
 * sobre H2 em memória; cada teste sofre rollback ao final.
 */
@DataJpaTest
@Import(ClienteRepositoryAdapter.class)
class ClienteRepositoryAdapterDataJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClienteRepositoryAdapter adapter;

    @Test
    @DisplayName("Salva e recupera o cliente pelo CPF")
    void salvarEBuscarPorCpf() {
        adapter.salvar(new Cliente("12345678901", "Ana"));
        entityManager.flush();
        entityManager.clear();

        assertThat(adapter.buscarPorCpf("12345678901")).contains(new Cliente("12345678901", "Ana"));
        assertThat(adapter.existePorCpf("12345678901")).isTrue();
        assertThat(adapter.existePorCpf("00000000000")).isFalse();
    }

    @Test
    @DisplayName("Busca por nome é parcial, ignora maiúsculas e ordena por nome")
    void buscarPorNome() {
        entityManager.persist(new ClienteEntity("11111111111", "Mariana"));
        entityManager.persist(new ClienteEntity("22222222222", "Bruno"));
        entityManager.persist(new ClienteEntity("33333333333", "Ana Maria"));
        entityManager.flush();

        assertThat(adapter.buscarPorNome("MARI"))
                .extracting(Cliente::cpf)
                .containsExactly("33333333333", "11111111111");
    }

    @Test
    @DisplayName("Lista todos ordenados por nome")
    void listarTodos() {
        entityManager.persist(new ClienteEntity("11111111111", "Carla"));
        entityManager.persist(new ClienteEntity("22222222222", "Bruno"));
        entityManager.flush();

        assertThat(adapter.listarTodos()).extracting(Cliente::nome).containsExactly("Bruno", "Carla");
    }
}
