package com.exemplo.clientes.dominio;

public class ClienteNaoEncontradoException extends RuntimeException {
    public ClienteNaoEncontradoException(String cpf) {
        super("Cliente com CPF " + cpf + " não encontrado");
    }
}
