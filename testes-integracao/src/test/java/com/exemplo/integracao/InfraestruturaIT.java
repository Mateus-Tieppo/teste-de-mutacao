package com.exemplo.integracao;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jayway.jsonpath.JsonPath;

class InfraestruturaIT extends SistemaNoAr {

    @Test
    @DisplayName("Gateway e os 3 microsserviços estão registrados e UP no Eureka")
    void servicosRegistradosNoEureka() {
        HttpResponse<String> apps = getAbsoluto(EUREKA + "/eureka/apps", "application/json");

        assertThat(apps.statusCode()).isEqualTo(200);
        List<String> registrados = JsonPath.read(apps.body(),
                "$.applications.application[?(@.instance[0].status == 'UP')].name");
        assertThat(registrados).contains("GATEWAY", "PECAS-SERVICE", "CLIENTES-SERVICE", "REPRESENTANTES-SERVICE");
    }

    @Test
    @DisplayName("O Gateway serve o front-end na raiz")
    void gatewayServeFrontEnd() {
        HttpResponse<String> pagina = get("/");

        assertThat(pagina.statusCode()).isEqualTo(200);
        assertThat(pagina.body()).contains("Cadastro via API Gateway");
    }
}
