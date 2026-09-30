package com.exemplo.integracao;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jayway.jsonpath.JsonPath;

class RepresentantesGatewayIT extends SistemaNoAr {

    @Test
    @DisplayName("Cadastra, consulta por CPF, por nome e lista representantes através do Gateway")
    void fluxoCompleto() {
        String cpf = cpfAleatorio();
        String nome = "Representante " + sufixoUnico();
        String json = """
                {"cpf":"%s","nome":"%s"}""".formatted(cpf, nome);

        assertThat(post("/representantes", json).statusCode()).isEqualTo(201);
        assertThat(post("/representantes", json).statusCode()).as("CPF duplicado").isEqualTo(409);

        HttpResponse<String> porCpf = get("/representantes/" + cpf);
        assertThat(porCpf.statusCode()).isEqualTo(200);
        assertThat((String) JsonPath.read(porCpf.body(), "$.nome")).isEqualTo(nome);

        HttpResponse<String> porNome = get("/representantes?nome=" + nome.toUpperCase().replace(' ', '+'));
        assertThat(porNome.statusCode()).isEqualTo(200);
        assertThat(JsonPath.<List<String>>read(porNome.body(), "$[*].cpf")).containsExactly(cpf);

        assertThat(JsonPath.<List<String>>read(get("/representantes").body(), "$[*].cpf")).contains(cpf);
    }

    @Test
    @DisplayName("CPF inexistente devolve 404 e CPF inválido devolve 400")
    void erros() {
        assertThat(get("/representantes/00000000000").statusCode()).isEqualTo(404);
        assertThat(post("/representantes", """
                {"cpf":"abc","nome":"X"}""").statusCode()).isEqualTo(400);
    }
}
