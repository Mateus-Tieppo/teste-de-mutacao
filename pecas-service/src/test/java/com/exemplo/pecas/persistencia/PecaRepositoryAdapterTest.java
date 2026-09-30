package com.exemplo.pecas.persistencia;

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

import com.exemplo.pecas.dominio.Peca;

/**
 * Teste unitário do código de persistência isolando o framework de
 * persistência e o banco: o Spring Data (PecaJpaRepository) é um mock,
 * então nenhum EntityManager, Hibernate ou H2 é iniciado.
 * Verifica a conversão domínio <-> entidade e a delegação correta.
 */
@ExtendWith(MockitoExtension.class)
class PecaRepositoryAdapterTest {

    @Mock
    private PecaJpaRepository jpa;

    @InjectMocks
    private PecaRepositoryAdapter adapter;

    @Captor
    private ArgumentCaptor<PecaEntity> entityCaptor;

    @Test
    @DisplayName("Salvar converte o domínio em entidade e a entidade salva de volta em domínio")
    void salvar_converteEDelega() {
        when(jpa.save(any(PecaEntity.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Peca resultado = adapter.salvar(new Peca(3L, "Rolamento", "Rolamento 6202"));

        verify(jpa).save(entityCaptor.capture());
        PecaEntity salva = entityCaptor.getValue();
        assertThat(salva.getId()).isEqualTo(3L);
        assertThat(salva.getNome()).isEqualTo("Rolamento");
        assertThat(salva.getDescricao()).isEqualTo("Rolamento 6202");
        assertThat(resultado).isEqualTo(new Peca(3L, "Rolamento", "Rolamento 6202"));
    }

    @Test
    @DisplayName("Buscar por id existente retorna a peça convertida")
    void buscarPorId_existente() {
        when(jpa.findById(1L)).thenReturn(Optional.of(new PecaEntity(1L, "Porca", "M8")));

        assertThat(adapter.buscarPorId(1L)).contains(new Peca(1L, "Porca", "M8"));
    }

    @Test
    @DisplayName("Buscar por id inexistente retorna vazio")
    void buscarPorId_inexistente() {
        when(jpa.findById(1L)).thenReturn(Optional.empty());

        assertThat(adapter.buscarPorId(1L)).isEmpty();
    }

    @Test
    @DisplayName("Buscar por nome usa a consulta derivada e converte todos os resultados")
    void buscarPorNome_converteLista() {
        when(jpa.findByNomeContainingIgnoreCaseOrderByNome("para")).thenReturn(List.of(
                new PecaEntity(1L, "Parafuso M6", "a"),
                new PecaEntity(2L, "Parafuso M8", "b")));

        assertThat(adapter.buscarPorNome("para")).containsExactly(
                new Peca(1L, "Parafuso M6", "a"),
                new Peca(2L, "Parafuso M8", "b"));
    }

    @Test
    @DisplayName("Listar todas usa a consulta ordenada por nome")
    void listarTodas_converteLista() {
        when(jpa.findAllByOrderByNome()).thenReturn(List.of(new PecaEntity(1L, "A", "a")));

        assertThat(adapter.listarTodas()).containsExactly(new Peca(1L, "A", "a"));
    }

    @Test
    @DisplayName("Existe por id delega para existsById")
    void existePorId_delega() {
        when(jpa.existsById(4L)).thenReturn(true);

        assertThat(adapter.existePorId(4L)).isTrue();
    }
}
