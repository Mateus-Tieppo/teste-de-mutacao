package com.exemplo.pecas.dominio;

public class PecaNaoEncontradaException extends RuntimeException {
    public PecaNaoEncontradaException(Long id) {
        super("Peça " + id + " não encontrada");
    }
}
