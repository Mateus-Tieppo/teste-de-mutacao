package com.exemplo.integracao;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jayway.jsonpath.JsonPath;

class ClientesGatewayIT extends SistemaNoAr {

    @Test
    @DisplayName("Cadastra, consulta por CPF, por nome e lista clientes através do Gateway")
    void fluxoCompleto() {
        String cpf = cpfAleatorio();
        String cpfComMascara = cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                + cpf.substring(6, 9) + "-" + cpf.substring(9);
        String nome = "Cliente " + sufixoUnico();
        String json = """
                {"cpf":"%s","nome":"%s"}""".formatted(cpfComMascara, nome);

        HttpResponse<String> cadastro = post("/clientes", json);
        assertThat(cadastro.statusCode()).isEqualTo(201);
        assertThat((String) JsonPath.read(cadastro.body(), "$.cpf")).as("CPF sem máscara").isEqualTo(cpf);

        assertThat(post("/clientes", json).statusCode()).as("CPF duplicado").isEqualTo(409);

        HttpResponse<String> porCpf = get("/clientes/" + cpf);
        assertThat(porCpf.statusCode()).isEqualTo(200);
        assertThat((String) JsonPath.read(porCpf.body(), "$.nome")).isEqualTo(nome);

        HttpResponse<String> porNome = get("/clientes?nome=" + nome.toLowerCase().replace(' ', '+'));
        assertThat(porNome.statusCode()).isEqualTo(200);
        assertThat(JsonPath.<List<String>>read(porNome.body(), "$[*].cpf")).containsExactly(cpf);

        assertThat(JsonPath.<List<String>>read(get("/clientes").body(), "$[*].cpf")).contains(cpf);
    }

    @Test
    @DisplayName("CPF inexistente devolve 404 e CPF inválido devolve 400")
    void erros() {
        assertThat(get("/clientes/00000000000").statusCode()).isEqualTo(404);
        assertThat(post("/clientes", """
                {"cpf":"123","nome":"X"}""").statusCode()).isEqualTo(400);
    }
}
