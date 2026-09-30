package com.exemplo.clientes.persistencia;

import com.exemplo.clientes.dominio.Cliente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
public class ClienteEntity {
    @Id
    @Column(length = 11)
    private String cpf;

    @Column(nullable = false)
    private String nome;

    protected ClienteEntity() {
    }

    public ClienteEntity(String cpf, String nome) {
        this.cpf = cpf;
        this.nome = nome;
    }

    public static ClienteEntity deDominio(Cliente cliente) {
        return new ClienteEntity(cliente.cpf(), cliente.nome());
    }

    public Cliente paraDominio() {
        return new Cliente(cpf, nome);
    }

    public String getCpf() {
        return cpf;
    }

    public String getNome() {
        return nome;
    }
}
