package com.exemplo.clientes.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, String> {
    List<ClienteEntity> findByNomeContainingIgnoreCaseOrderByNome(String nome);

    List<ClienteEntity> findAllByOrderByNome();
}
