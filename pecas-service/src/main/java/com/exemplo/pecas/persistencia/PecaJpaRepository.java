package com.exemplo.pecas.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PecaJpaRepository extends JpaRepository<PecaEntity, Long> {
    List<PecaEntity> findByNomeContainingIgnoreCaseOrderByNome(String nome);

    List<PecaEntity> findAllByOrderByNome();
}
