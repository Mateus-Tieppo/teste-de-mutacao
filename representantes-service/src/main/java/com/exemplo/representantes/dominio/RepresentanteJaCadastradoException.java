package com.exemplo.representantes.dominio;

public class RepresentanteJaCadastradoException extends RuntimeException {
    public RepresentanteJaCadastradoException(String cpf) {
        super("Representante com CPF " + cpf + " já cadastrado");
    }
}
