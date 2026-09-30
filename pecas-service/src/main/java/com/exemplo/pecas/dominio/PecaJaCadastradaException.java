package com.exemplo.pecas.dominio;

public class PecaJaCadastradaException extends RuntimeException {
    public PecaJaCadastradaException(Long id) {
        super("Peça " + id + " já cadastrada");
    }
}
