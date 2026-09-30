package com.exemplo.pecas.persistencia;

import com.exemplo.pecas.dominio.Peca;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pecas")
public class PecaEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, length = 1000)
    private String descricao;

    protected PecaEntity() {
    }

    public PecaEntity(Long id, String nome, String descricao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
    }

    public static PecaEntity deDominio(Peca peca) {
        return new PecaEntity(peca.id(), peca.nome(), peca.descricao());
    }

    public Peca paraDominio() {
        return new Peca(id, nome, descricao);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
}
