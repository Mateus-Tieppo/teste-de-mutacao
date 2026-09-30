package com.exemplo.pecas.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.exemplo.pecas.dominio.PecaJaCadastradaException;
import com.exemplo.pecas.dominio.PecaNaoEncontradaException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(PecaNaoEncontradaException.class)
    public ProblemDetail naoEncontrada(PecaNaoEncontradaException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(PecaJaCadastradaException.class)
    public ProblemDetail duplicada(PecaJaCadastradaException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalida(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
