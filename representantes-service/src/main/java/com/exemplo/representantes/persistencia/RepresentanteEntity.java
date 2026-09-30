package com.exemplo.representantes.persistencia;

import com.exemplo.representantes.dominio.Representante;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "representantes")
public class RepresentanteEntity {
    @Id
    @Column(length = 11)
    private String cpf;

    @Column(nullable = false)
    private String nome;

    protected RepresentanteEntity() {
    }

    public RepresentanteEntity(String cpf, String nome) {
        this.cpf = cpf;
        this.nome = nome;
    }

    public static RepresentanteEntity deDominio(Representante representante) {
        return new RepresentanteEntity(representante.cpf(), representante.nome());
    }

    public Representante paraDominio() {
        return new Representante(cpf, nome);
    }

    public String getCpf() {
        return cpf;
    }

    public String getNome() {
        return nome;
    }
}
