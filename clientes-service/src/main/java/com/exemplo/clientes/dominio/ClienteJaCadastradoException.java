package com.exemplo.clientes.dominio;

public class ClienteJaCadastradoException extends RuntimeException {
    public ClienteJaCadastradoException(String cpf) {
        super("Cliente com CPF " + cpf + " já cadastrado");
    }
}
