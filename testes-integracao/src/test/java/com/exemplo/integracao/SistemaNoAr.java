package com.exemplo.integracao;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeAll;

/**
 * Base dos testes fim a fim: faz requisições HTTP reais ao Gateway.
 * Se o sistema não estiver no ar, os testes são ignorados (não falham).
 */
abstract class SistemaNoAr {
    static final String GATEWAY = System.getProperty("gateway.url", "http://localhost:8080");
    static final String EUREKA = System.getProperty("eureka.url", "http://localhost:8761");

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    @BeforeAll
    static void exigeSistemaNoAr() {
        assumeTrue(gatewayRoteando(),
                "Sistema fora do ar em " + GATEWAY + ". Suba config-server, discovery-server, "
                        + "os 3 serviços e o gateway antes de rodar: mvn -pl testes-integracao verify");
    }

    /**
     * O Gateway só consegue rotear depois que os serviços aparecem no Eureka,
     * então espera até as 3 rotas responderem (ou desiste após ~30s).
     */
    private static boolean gatewayRoteando() {
        for (int tentativa = 0; tentativa < 15; tentativa++) {
            try {
                if (get("/pecas").statusCode() == 200
                        && get("/clientes").statusCode() == 200
                        && get("/representantes").statusCode() == 200) {
                    return true;
                }
            } catch (Exception foraDoAr) {
                return false;
            }
            dormir();
        }
        return false;
    }

    static HttpResponse<String> get(String caminho) {
        return enviar(HttpRequest.newBuilder(URI.create(GATEWAY + caminho)).GET());
    }

    static HttpResponse<String> getAbsoluto(String url, String accept) {
        return enviar(HttpRequest.newBuilder(URI.create(url)).header("Accept", accept).GET());
    }

    static HttpResponse<String> post(String caminho, String json) {
        return enviar(HttpRequest.newBuilder(URI.create(GATEWAY + caminho))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)));
    }

    /** CPF aleatório (11 dígitos) para não colidir com dados de execuções anteriores. */
    static String cpfAleatorio() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000_000L, 99_999_999_999L));
    }

    /** Texto único para as buscas por nome encontrarem só o registro deste teste. */
    static String sufixoUnico() {
        return Long.toString(System.nanoTime(), 36);
    }

    private static HttpResponse<String> enviar(HttpRequest.Builder requisicao) {
        try {
            return HTTP.send(requisicao.timeout(Duration.ofSeconds(10)).build(),
                    HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao chamar o sistema: " + e.getMessage(), e);
        }
    }

    private static void dormir() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
