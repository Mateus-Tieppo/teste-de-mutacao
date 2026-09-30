package com.exemplo.representantes.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.exemplo.representantes.dominio.Representante;
import com.exemplo.representantes.dominio.RepresentanteRepository;

/**
 * Adaptador que implementa a porta do domínio usando Spring Data JPA,
 * convertendo entre a entidade JPA e o modelo de domínio.
 */
@Repository
public class RepresentanteRepositoryAdapter implements RepresentanteRepository {
    private final RepresentanteJpaRepository jpa;

    public RepresentanteRepositoryAdapter(RepresentanteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Representante salvar(Representante representante) {
        return jpa.save(RepresentanteEntity.deDominio(representante)).paraDominio();
    }

    @Override
    public Optional<Representante> buscarPorCpf(String cpf) {
        return jpa.findById(cpf).map(RepresentanteEntity::paraDominio);
    }

    @Override
    public List<Representante> buscarPorNome(String nome) {
        return jpa.findByNomeContainingIgnoreCaseOrderByNome(nome).stream()
                .map(RepresentanteEntity::paraDominio)
                .toList();
    }

    @Override
    public List<Representante> listarTodos() {
        return jpa.findAllByOrderByNome().stream()
                .map(RepresentanteEntity::paraDominio)
                .toList();
    }

    @Override
    public boolean existePorCpf(String cpf) {
        return jpa.existsById(cpf);
    }
}
