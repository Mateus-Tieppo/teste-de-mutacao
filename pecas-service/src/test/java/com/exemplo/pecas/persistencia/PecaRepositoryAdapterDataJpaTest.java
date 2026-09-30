package com.exemplo.pecas.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.exemplo.pecas.dominio.Peca;

/**
 * Teste de integração da fatia de persistência (@DataJpaTest): JPA real
 * sobre H2 em memória; cada teste sofre rollback ao final.
 */
@DataJpaTest
@Import(PecaRepositoryAdapter.class)
class PecaRepositoryAdapterDataJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PecaRepositoryAdapter adapter;

    @Test
    @DisplayName("Salva e recupera a peça pelo id")
    void salvarEBuscarPorId() {
        adapter.salvar(new Peca(1L, "Parafuso", "M8"));
        entityManager.flush();
        entityManager.clear();

        assertThat(adapter.buscarPorId(1L)).contains(new Peca(1L, "Parafuso", "M8"));
        assertThat(adapter.existePorId(1L)).isTrue();
        assertThat(adapter.existePorId(2L)).isFalse();
    }

    @Test
    @DisplayName("Busca por nome é parcial, ignora maiúsculas e ordena por nome")
    void buscarPorNome() {
        entityManager.persist(new PecaEntity(1L, "Parafuso M8", "a"));
        entityManager.persist(new PecaEntity(2L, "Porca", "b"));
        entityManager.persist(new PecaEntity(3L, "Parafuso M6", "c"));
        entityManager.flush();

        assertThat(adapter.buscarPorNome("PARAF"))
                .extracting(Peca::id)
                .containsExactly(3L, 1L);
    }

    @Test
    @DisplayName("Lista todas ordenadas por nome")
    void listarTodas() {
        entityManager.persist(new PecaEntity(1L, "Mola", "a"));
        entityManager.persist(new PecaEntity(2L, "Arruela", "b"));
        entityManager.flush();

        assertThat(adapter.listarTodas()).extracting(Peca::nome).containsExactly("Arruela", "Mola");
    }
}
