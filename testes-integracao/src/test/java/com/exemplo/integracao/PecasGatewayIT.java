package com.exemplo.integracao;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jayway.jsonpath.JsonPath;

class PecasGatewayIT extends SistemaNoAr {

    @Test
    @DisplayName("Cadastra, consulta por id, por nome e lista peças através do Gateway")
    void fluxoCompleto() {
        long id = ThreadLocalRandom.current().nextLong(1_000_000, Long.MAX_VALUE / 2);
        String nome = "Engrenagem " + sufixoUnico();
        String json = """
                {"id":%d,"nome":"%s","descricao":"Engrenagem 20 dentes"}""".formatted(id, nome);

        HttpResponse<String> cadastro = post("/pecas", json);
        assertThat(cadastro.statusCode()).isEqualTo(201);
        assertThat(cadastro.headers().firstValue("Location")).contains("/pecas/" + id);

        assertThat(post("/pecas", json).statusCode()).as("id duplicado").isEqualTo(409);

        HttpResponse<String> porId = get("/pecas/" + id);
        assertThat(porId.statusCode()).isEqualTo(200);
        assertThat((String) JsonPath.read(porId.body(), "$.nome")).isEqualTo(nome);
        assertThat((String) JsonPath.read(porId.body(), "$.descricao")).isEqualTo("Engrenagem 20 dentes");

        HttpResponse<String> porNome = get("/pecas?nome=" + nome.toUpperCase().replace(' ', '+'));
        assertThat(porNome.statusCode()).isEqualTo(200);
        List<Number> ids = JsonPath.read(porNome.body(), "$[*].id");
        assertThat(ids).extracting(Number::longValue).containsExactly(id);

        List<Number> todos = JsonPath.read(get("/pecas").body(), "$[*].id");
        assertThat(todos).extracting(Number::longValue).contains(id);
    }

    @Test
    @DisplayName("Peça inexistente devolve 404 e peça inválida devolve 400")
    void erros() {
        assertThat(get("/pecas/" + Long.MAX_VALUE).statusCode()).isEqualTo(404);
        assertThat(post("/pecas", """
                {"id":-1,"nome":"X","descricao":"Y"}""").statusCode()).isEqualTo(400);
    }
}
