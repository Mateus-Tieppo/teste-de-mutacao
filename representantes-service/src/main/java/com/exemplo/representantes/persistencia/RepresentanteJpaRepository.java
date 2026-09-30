package com.exemplo.representantes.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RepresentanteJpaRepository extends JpaRepository<RepresentanteEntity, String> {
    List<RepresentanteEntity> findByNomeContainingIgnoreCaseOrderByNome(String nome);

    List<RepresentanteEntity> findAllByOrderByNome();
}
