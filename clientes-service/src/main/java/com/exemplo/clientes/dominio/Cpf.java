package com.exemplo.clientes.dominio;

public final class Cpf {
    private Cpf() {
    }

    /** Remove a máscara ("123.456.789-01" -> "12345678901") e exige 11 dígitos. */
    public static String normalizar(String cpf) {
        if (cpf == null) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }
        String digitos = cpf.replaceAll("[.\\-\\s]", "");
        if (!digitos.matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF deve conter 11 dígitos");
        }
        return digitos;
    }
}
