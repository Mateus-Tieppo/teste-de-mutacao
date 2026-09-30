package com.exemplo.representantes.dominio;

import java.util.List;
import java.util.Optional;

/**
 * Porta de persistência: o serviço depende desta interface,
 * não do Spring Data/JPA nem do banco.
 */
public interface RepresentanteRepository {
    Representante salvar(Representante representante);

    Optional<Representante> buscarPorCpf(String cpf);

    List<Representante> buscarPorNome(String nome);

    List<Representante> listarTodos();

    boolean existePorCpf(String cpf);
}
