package com.exemplo.pecas.dominio;

import java.util.List;
import java.util.Optional;

/**
 * Porta de persistência: o serviço depende desta interface,
 * não do Spring Data/JPA nem do banco.
 */
public interface PecaRepository {
    Peca salvar(Peca peca);

    Optional<Peca> buscarPorId(Long id);

    List<Peca> buscarPorNome(String nome);

    List<Peca> listarTodas();

    boolean existePorId(Long id);
}
