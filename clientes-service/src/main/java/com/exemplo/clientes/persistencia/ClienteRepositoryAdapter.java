package com.exemplo.clientes.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.exemplo.clientes.dominio.Cliente;
import com.exemplo.clientes.dominio.ClienteRepository;

/**
 * Adaptador que implementa a porta do domínio usando Spring Data JPA,
 * convertendo entre a entidade JPA e o modelo de domínio.
 */
@Repository
public class ClienteRepositoryAdapter implements ClienteRepository {
    private final ClienteJpaRepository jpa;

    public ClienteRepositoryAdapter(ClienteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        return jpa.save(ClienteEntity.deDominio(cliente)).paraDominio();
    }

    @Override
    public Optional<Cliente> buscarPorCpf(String cpf) {
        return jpa.findById(cpf).map(ClienteEntity::paraDominio);
    }

    @Override
    public List<Cliente> buscarPorNome(String nome) {
        return jpa.findByNomeContainingIgnoreCaseOrderByNome(nome).stream()
                .map(ClienteEntity::paraDominio)
                .toList();
    }

    @Override
    public List<Cliente> listarTodos() {
        return jpa.findAllByOrderByNome().stream()
                .map(ClienteEntity::paraDominio)
                .toList();
    }

    @Override
    public boolean existePorCpf(String cpf) {
        return jpa.existsById(cpf);
    }
}
