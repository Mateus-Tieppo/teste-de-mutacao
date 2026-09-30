package com.exemplo.pecas.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.exemplo.pecas.dominio.Peca;
import com.exemplo.pecas.dominio.PecaRepository;

/**
 * Adaptador que implementa a porta do domínio usando Spring Data JPA,
 * convertendo entre a entidade JPA e o modelo de domínio.
 */
@Repository
public class PecaRepositoryAdapter implements PecaRepository {
    private final PecaJpaRepository jpa;

    public PecaRepositoryAdapter(PecaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Peca salvar(Peca peca) {
        return jpa.save(PecaEntity.deDominio(peca)).paraDominio();
    }

    @Override
    public Optional<Peca> buscarPorId(Long id) {
        return jpa.findById(id).map(PecaEntity::paraDominio);
    }

    @Override
    public List<Peca> buscarPorNome(String nome) {
        return jpa.findByNomeContainingIgnoreCaseOrderByNome(nome).stream()
                .map(PecaEntity::paraDominio)
                .toList();
    }

    @Override
    public List<Peca> listarTodas() {
        return jpa.findAllByOrderByNome().stream()
                .map(PecaEntity::paraDominio)
                .toList();
    }

    @Override
    public boolean existePorId(Long id) {
        return jpa.existsById(id);
    }
}
