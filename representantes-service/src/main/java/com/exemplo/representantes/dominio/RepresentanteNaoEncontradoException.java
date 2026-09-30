package com.exemplo.representantes.dominio;

public class RepresentanteNaoEncontradoException extends RuntimeException {
    public RepresentanteNaoEncontradoException(String cpf) {
        super("Representante com CPF " + cpf + " não encontrado");
    }
}
