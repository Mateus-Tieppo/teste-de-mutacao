# Experimentação: microserviços com Spring Cloud, observabilidade e testes

Microserviços de **peças**, **clientes** e **representantes comerciais**, com Gateway,
Service Discovery (Eureka) e Configuração Centralizada (Config Server). As métricas são
expostas ao Prometheus e visualizadas no Grafana. Cada serviço tem testes unitários e de integração.

| Módulo                   | Porta | Papel                                                   |
|--------------------------|-------|---------------------------------------------------------|
| `config-server`          | 8888  | Configuração centralizada (`src/main/resources/config`) |
| `discovery-server`       | 8761  | Eureka (Service Discovery)                              |
| `gateway`                | 8080  | Spring Cloud Gateway, **único ponto de acesso**         |
| `pecas-service`          | 8081  | Peças (id, nome, descrição)                             |
| `clientes-service`       | 8082  | Clientes (CPF, nome)                                    |
| `representantes-service` | 8083  | Representantes comerciais (CPF, nome)                   |
| Prometheus (Docker)      | 9090  | Coleta `/actuator/prometheus` de todos os serviços      |
| Grafana (Docker)         | 3000  | Dashboard provisionado (admin/admin)                    |

## Organização de cada microserviço

```
dominio/       Peca (record), PecaRepository (porta), exceções      -> sem framework
aplicacao/     PecaService (regras + métrica pecas_cadastradas)     -> depende só da porta
persistencia/  PecaEntity, PecaJpaRepository (Spring Data),
               PecaRepositoryAdapter (implementa a porta)            -> JPA/H2
web/           PecaController, ApiExceptionHandler                   -> Spring MVC
```

O serviço depende da interface `PecaRepository`, e não do Spring Data. É isso que
permite testar cada camada isolada das outras.

## Testes

| Tipo           | Classe (peças; clientes e representantes são análogas) | O que é isolado |
|----------------|--------------------------------------------------------|-----------------|
| Unitário       | `aplicacao/PecaServiceTest`                  | Repositório mockado (Mockito). Sem Spring, JPA ou BD |
| Unitário       | `web/PecaControllerTest`                     | **Framework web isolado**: métodos do controller chamados direto, sem MockMvc/servlet/contexto Spring |
| Unitário       | `persistencia/PecaRepositoryAdapterTest`     | **Framework de persistência e BD isolados**: `PecaJpaRepository` mockado, sem Hibernate/H2 |
| Integração     | `web/PecaControllerWebMvcTest`               | `@WebMvcTest` + MockMvc; serviço como `@MockitoBean` |
| Integração     | `persistencia/PecaRepositoryAdapterDataJpaTest` | `@DataJpaTest`: JPA real em H2, rollback por teste |
| Integração     | `PecasIntegracaoTest`                        | `@SpringBootTest` completo + confere a métrica no `/actuator/prometheus` |

> No Spring Boot 3.4+ o `@MockBean` do material de apoio está depreciado; o equivalente é `@MockitoBean`.

Rodar todos os testes (não precisa de Config Server, Eureka nem Docker; `src/test/resources/application.yml` os desliga):

```bash
mvn test
```

## Executando o sistema

Requer JDK 21+ (Maven e `java` precisam apontar para ele).

```bash
mvn package -DskipTests

# nesta ordem, cada um em um terminal:
java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar
java -jar discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar
java -jar pecas-service/target/pecas-service-0.0.1-SNAPSHOT.jar
java -jar clientes-service/target/clientes-service-0.0.1-SNAPSHOT.jar
java -jar representantes-service/target/representantes-service-0.0.1-SNAPSHOT.jar
java -jar gateway/target/gateway-0.0.1-SNAPSHOT.jar

# Prometheus + Grafana
docker compose up -d
```

Após subir, o Gateway pode levar ~30 s para enxergar os serviços no Eureka (antes disso responde 503).

### Chamadas pelo Gateway

```bash
curl -X POST localhost:8080/pecas -H 'Content-Type: application/json' \
     -d '{"id":1,"nome":"Parafuso","descricao":"Parafuso M8"}'
curl localhost:8080/pecas                  # listar todas
curl localhost:8080/pecas/1                # por id
curl 'localhost:8080/pecas?nome=para'      # por nome (parcial, sem diferenciar maiúsculas)

curl -X POST localhost:8080/clientes -H 'Content-Type: application/json' \
     -d '{"cpf":"123.456.789-01","nome":"Ana Souza"}'
curl localhost:8080/clientes/12345678901
curl 'localhost:8080/clientes?nome=ana'

curl -X POST localhost:8080/representantes -H 'Content-Type: application/json' \
     -d '{"cpf":"98765432100","nome":"Carlos Lima"}'
curl localhost:8080/representantes
```

Erros: 400 (dados inválidos), 404 (não encontrado), 409 (já cadastrado), no formato `ProblemDetail`.

### Observabilidade

- Prometheus: http://localhost:9090 (Status → Targets deve mostrar os 4 alvos `UP`)
- Grafana: http://localhost:3000, dashboard **Microserviços - Peças, Clientes e Representantes**,
  com requisições/s, latência p95, erros 4xx/5xx, heap da JVM, serviços no ar e as métricas
  de negócio `pecas_cadastradas_total`, `clientes_cadastrados_total` e `representantes_cadastrados_total`.
# AtividadeConstru-o
