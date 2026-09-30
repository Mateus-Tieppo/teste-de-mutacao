# Microsserviços com Spring Cloud: Peças, Clientes e Representantes

Sistema para cadastro e consulta de **peças**, **clientes** e **representantes comerciais**, dividido em 3 microsserviços. Ele implementa os padrões **Gateway**, **Service Discovery** e **Configuração Centralizada** com Spring Cloud. Também tem **observabilidade** (Prometheus + Grafana) e **testes unitários, de integração e de mutação**.

## Arquitetura

```
 Navegador / Postman
         │
         ▼
   Gateway :8080  ──────────── serve também o front-end (http://localhost:8080/)
         │  lb://  (resolve pelo Eureka)
   ┌─────┼──────────────┐
   ▼     ▼              ▼
 Peças  Clientes   Representantes        Discovery Server (Eureka) :8761
 :8081  :8082      :8083                 Config Server :8888
 (H2)   (H2)       (H2)

 Prometheus :9090  ──coleta /actuator/prometheus──▶ gateway e serviços
 Grafana    :3000  ──lê o Prometheus──▶ dashboard pronto
```

| Módulo | Porta | Papel |
|---|---|---|
| `config-server` | 8888 | Configuração centralizada (modo `native`, arquivos em `config-server/src/main/resources/config`) |
| `discovery-server` | 8761 | Service Discovery (Eureka) |
| `gateway` | 8080 | API Gateway (Spring Cloud Gateway), **único ponto de acesso**, e front-end |
| `pecas-service` | 8081 | Microsserviço de peças |
| `clientes-service` | 8082 | Microsserviço de clientes |
| `representantes-service` | 8083 | Microsserviço de representantes |
| `testes-integracao` | – | Testes fim a fim que passam pelo Gateway |
| Prometheus / Grafana | 9090 / 3000 | Métricas e dashboards (via Docker) |

Os três serviços de negócio usam a mesma organização em camadas:

```
dominio/       modelo (record), exceções e a porta do repositório (interface)
aplicacao/     serviço com as regras de negócio (validação, duplicidade, métricas)
persistencia/  adaptador que implementa a porta com Spring Data JPA + entidade JPA
web/           controller REST e tratamento de erros (ProblemDetail)
```

O serviço depende só da interface `...Repository` do domínio. Por isso os testes unitários isolam o Spring, o JPA e o banco de dados.

## Como executar

**Pré-requisitos:** Java 21, Maven 3.9+ e Docker (só para Prometheus/Grafana).

```bash
mvn clean package -DskipTests
```

Suba nesta ordem, cada um em um terminal:

```bash
java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar          # 1º
java -jar discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar    # 2º
java -jar pecas-service/target/pecas-service-0.0.1-SNAPSHOT.jar
java -jar clientes-service/target/clientes-service-0.0.1-SNAPSHOT.jar
java -jar representantes-service/target/representantes-service-0.0.1-SNAPSHOT.jar
java -jar gateway/target/gateway-0.0.1-SNAPSHOT.jar                      # por último
```

Observabilidade:

```bash
docker compose up -d
```

| Endereço | O que é |
|---|---|
| http://localhost:8080 | **Front-end** (acessa só o Gateway) |
| http://localhost:8761 | Painel do Eureka |
| http://localhost:8888/pecas-service/default | Configuração servida pelo Config Server |
| http://localhost:9090 | Prometheus (em *Status → Targets* os 4 alvos devem estar `UP`) |
| http://localhost:3000 | Grafana (`admin` / `admin`), dashboard *Microserviços - Peças, Clientes e Representantes* |

> Depois que os serviços sobem, o Gateway pode levar alguns segundos para enxergá-los no Eureka. Até lá ele responde `503`.

## API (sempre pelo Gateway, porta 8080)

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/pecas` | Cadastra peça `{"id": 1, "nome": "...", "descricao": "..."}` |
| `GET` | `/pecas` | Lista todas as peças |
| `GET` | `/pecas?nome=paraf` | Consulta por nome (parcial, sem diferenciar maiúsculas) |
| `GET` | `/pecas/{id}` | Consulta por número de identificação |
| `POST` | `/clientes` | Cadastra cliente `{"cpf": "123.456.789-01", "nome": "..."}` |
| `GET` | `/clientes` | Lista todos os clientes |
| `GET` | `/clientes?nome=joao` | Consulta por nome |
| `GET` | `/clientes/{cpf}` | Consulta por CPF (com ou sem máscara) |
| `POST` | `/representantes` | Cadastra representante `{"cpf": "...", "nome": "..."}` |
| `GET` | `/representantes` | Lista todos os representantes |
| `GET` | `/representantes?nome=maria` | Consulta por nome |
| `GET` | `/representantes/{cpf}` | Consulta por CPF |

Respostas de erro vêm em formato `ProblemDetail`: `400` para dados inválidos, `404` para registro não encontrado e `409` para id/CPF já cadastrado.

```bash
curl -X POST http://localhost:8080/pecas -H "Content-Type: application/json" \
     -d '{"id": 1, "nome": "Parafuso M8", "descricao": "Parafuso sextavado de aço inox"}'
curl "http://localhost:8080/pecas?nome=paraf"
curl -X POST http://localhost:8080/clientes -H "Content-Type: application/json" \
     -d '{"cpf": "123.456.789-01", "nome": "João Silva"}'
curl http://localhost:8080/clientes/12345678901
```

## Observabilidade

- Cada serviço e o Gateway expõem métricas em `/actuator/prometheus` (Actuator + Micrometer), com a tag `application`.
- Métricas de negócio: `pecas_cadastradas_total`, `clientes_cadastrados_total` e `representantes_cadastrados_total`.
- O `docker-compose.yml` sobe o Prometheus (configurado em `observabilidade/prometheus/prometheus.yml`) e o Grafana, com o datasource e o dashboard já provisionados (`observabilidade/grafana`).
- O dashboard mostra requisições/s, latência p95, erros 4xx/5xx, cadastros, memória heap e quais serviços estão no ar.

## Testes

São quatro tipos de teste, cada um com um objetivo diferente:

| Tipo | Classes | O que isola | Como rodar |
|---|---|---|---|
| **Unitário: serviço** | `*ServiceTest` | Repositório mockado (Mockito). Sem Spring, sem JPA, sem banco | `mvn test` |
| **Unitário: controller** | `*ControllerTest` | Framework web isolado: métodos chamados direto, serviço mockado | `mvn test` |
| **Unitário: persistência** | `*RepositoryAdapterTest` | Framework de persistência e banco isolados: Spring Data mockado | `mvn test` |
| Integração: fatias | `*WebMvcTest`, `*DataJpaTest` | Só a camada web (MockMvc) ou só JPA + H2 | `mvn test` |
| Integração: serviço completo | `*IntegracaoTest` | Controller → serviço → JPA → H2, sem mocks, incluindo a métrica no Prometheus | `mvn test` |
| **Mutação (Pitest)** | todos os acima, exceto `*IntegracaoTest` | Mede se os testes detectam bugs de verdade | ver abaixo |
| **Fim a fim** | `testes-integracao/*IT` | Nada é mockado: sistema real pelo Gateway + Eureka + Config | ver abaixo |

`mvn test` não precisa de Config Server, Eureka nem Docker, porque o `src/test/resources/application.yml` de cada serviço os desliga.

> No Spring Boot 3.4+, o `@MockBean` do material de apoio está depreciado. O equivalente é `@MockitoBean`.

### Teste de mutação (Pitest)

O Pitest cria **mutantes** do código de produção (troca `<=` por `<`, remove uma chamada, faz um método devolver `null` etc.) e roda os testes contra cada um. Se algum teste falhar, o mutante foi **morto**. Se todos passarem, ele **sobreviveu**, o que indica um caso que os testes não verificam.

```bash
mvn -pl pecas-service,clientes-service,representantes-service test-compile org.pitest:pitest-maven:mutationCoverage
```

- Relatório HTML: `<serviço>/target/pit-reports/index.html`
- Mutadores: grupo `STRONGER`. Alvo: todas as classes do serviço, exceto a classe `*Application`.
- O build falha se o *mutation score* de algum serviço ficar abaixo de **80%** (`mutationThreshold`).

Resultado atual:

| Serviço | Mutantes gerados | Mortos | Mutation score | Cobertura de linhas |
|---|---|---|---|---|
| pecas-service | 47 | 47 | **100%** | 100% |
| clientes-service | 41 | 41 | **100%** | 97% |
| representantes-service | 41 | 41 | **100%** | 97% |

### Testes fim a fim (módulo `testes-integracao`)

São testes de caixa-preta. Eles fazem requisições HTTP reais ao Gateway e verificam para os 3 domínios: cadastro, duplicidade (409), consulta por id/CPF e por nome, listagem e erros (400/404). Também conferem que os serviços estão registrados no Eureka e que o Gateway serve o front-end.

```bash
# com o sistema no ar (veja "Como executar")
mvn -pl testes-integracao verify
```

Eles rodam só em `mvn verify`, com o Failsafe e o sufixo `*IT`. Se o sistema estiver fora do ar, são **ignorados** em vez de falhar. Para apontar para outro endereço, use `-Dgateway.url=... -Deureka.url=...`.
