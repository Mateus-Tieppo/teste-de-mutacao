package com.exemplo.clientes.dominio;

import java.util.List;
import java.util.Optional;

/**
 * Porta de persistência: o serviço depende desta interface,
 * não do Spring Data/JPA nem do banco.
 */
public interface ClienteRepository {
    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorCpf(String cpf);

    List<Cliente> buscarPorNome(String nome);

    List<Cliente> listarTodos();

    boolean existePorCpf(String cpf);
}
