# Research: To-Do List App

**Data**: 2026-09-11
**Escopo**: Resolver os pontos técnicos desconhecidos do plano (Phase 0) para a
stack declarada: front-end Vanilla JS, API REST Spring Boot (Java), SQLite,
Docker Compose.

## 1. Integração SQLite + Spring Boot

- **Decision**: Usar o driver `org.xerial:sqlite-jdbc` (empacota as libs
  nativas em um único JAR) com `spring-boot-starter-jdbc` e `JdbcTemplate`.
  Inicialização do schema via `schema.sql` idempotente com
  `spring.sql.init.mode=always`; por conexão, executar `PRAGMA journal_mode=WAL`,
  `PRAGMA synchronous=NORMAL` e `PRAGMA foreign_keys=ON` via
  `connection-init-sql` do HikariCP.
- **Rationale**: `sqlite-jdbc` é zero-config; JdbcTemplate dá controle total
  do SQL sem overhead de ORM. WAL melhora concorrência de leitura/escrita
  (grava­ções no SQLite são serializadas); foreign keys importam para
  integridade. `schema.sql` com `CREATE TABLE IF NOT EXISTS` é o bootstrap mais
  simples e idempotente (não é executado por padrão para não-embarcado, daí o
  `mode=always`).
- **Alternatives considered**: JPA/Hibernate — rejeitado: não há dialect
  oficial do SQLite, workarounds frágeis e cache de entidades conflita com a
  concorrência limitada do SQLite. Spring Data JDBC — rejeitado: sem suporte
  nativo a SQLite. Inicialização via `CommandLineRunner` sob demanda — funciona,
  porém é mais código que `schema.sql`.

## 2. CORS e topologia de rede

- **Decision**: Não configurar CORS no back-end. O contêiner `nginx`
  (front-end) serve os estáticos e faz proxy reverso de `/api/*` para o
  back-end Spring Boot, tornando toda a aplicação same-origin.
- **Rationale**: Com front-end e API no mesmo origin (proxy), os navegadores
  não exigem CORS; elimina cabeçalhos, pré-flights e credenciais do meio do
  caminho. Configuração do nginx (~6 linhas) é mais simples e robusta que um
  bean `WebMvcConfigurer` permanente.
- **Alternatives considered**: Bean `WebMvcConfigurer` global com
  `allowedOrigins("http://localhost:PORT")` + métodos
  GET/POST/PUT/DELETE/OPTIONS — válido, mas exige sincronizar origens e
  aumenta a superfície; `@CrossOrigin` por controller — repetitivo.

## 3. Estratégia de testes

- **Decision**: Back-end com JUnit 5 + `@SpringBootTest` + MockMvc + JsonPath
  (um teste por endpoint e por regra de negócio); front-end e integração
  full-stack com **Playwright** (E2E dirigindo navegador real contra a stack em
  Docker Compose).
- **Rationale**: MockMvc cobre toda a semântica REST/controller/service sem
  subir servidor real; Playwright cobre os fluxos visíveis ao usuário (criar,
  editar, marcar concluída, excluir, persistência e ordenação) com auto-wait e
  assertions retry nativos, provando a integração real com fetch, nginx, API e
  SQLite. Uma única ferramenta E2E mantém a suíte simples e satisfaz o
  requisito constitucional de testes automatizados.
- **Alternatives considered**: Vitest+jsdom — rápido para unidades, mas jsdom
  não cobre ~20% das APIs web e não prova a integração real do fetch/UI;
  limitado a lógica pura, se necessário. Cypress — funcional, porém com mais
  configuração que Playwright.

## 4. Docker e Docker Compose

- **Decision**: Back-end com build multi-stage (estágio 1: `eclipse-temurin:21-jdk`
  roda `mvn package`; estágio 2: `eclipse-temurin:21-jre` slim, usuário
  não-root, fat jar em camadas). Front-end: `nginx:alpine` copiando os arquivos
  estáticos (sem estágio de build — Vanilla JS não compila). Persistência do
  SQLite em **volume nomeado** (ex.: `/data/todo.db`). `compose.yml` sem campo
  version, com `restart: unless-stopped` e healthcheck do back-end.
- **Rationale**: JRE slim + multi-stage reduz a imagem ~50%; `nginx:alpine` é o
  servidor estático mais enxuto; volume nomeado sobrevive a `docker compose
  up/down` e regen­eração, sendo mais fácil de fazer backup que bind mount.
- **Alternatives considered**: Bind mount do banco — acopla dados a caminhos e
  permissões do host. Contêiner único (tudo em um) — mais simples porém acopla
  UI e API, contrariando G4.

## 5. Versões e dependências

- **Decision**: Java 21 (LTS) com Spring Boot na linha estável atual com
  suporte OSS ativo (Spring Boot 4.x / 4.1 na data da pesquisa). Dependências
  mínimas: `spring-boot-starter-web`, `spring-boot-starter-jdbc`,
  `org.xerial:sqlite-jdbc`; dev/test: `spring-boot-starter-test`. Sem Lombok.
  Manter a separação `@RestController` / `@Service` / `@Repository` (três
  classes pequenas), alinhada a convenções e à qualidade do código.
- **Rationale**: Iniciar projeto novo já aposentado da linha 3.x (suporte OSS
  encerrado em 2026-06-30) não é prudente; Java 21 tem suporte e funciona bem
  nas duas linhas. A separação em três camadas custa quase nada, melhora
  testabilidade e segue convenções.
- **Alternatives considered**: Spring Boot 3.5.x — último patch da linha 3.x,
  porém sem suporte OSS ativo. Lombok — mais uma ferramenta de processamento
  para três classes sem ganho real.

## Consolidação

| Decisão | Alternativa rejeitada | Motivo |
|---------|----------------------|--------|
| JdbcTemplate + sqlite-jdbc | JPA/Hibernate, Spring Data JDBC | Sem dialect oficial; overhead desnecessário (G1) |
| Proxy reverso nginx (same-origin) | CORS bean no Spring | Menos código, sem exposição desnecessária |
| MockMvc + Playwright | Vitest/jsdom, Cypress | Cobertura real do fluxo com 1 ferramenta E2E |
| Multi-stage temurin + volume nomeado | Bind mount, contêiner único | Menor imagem, persistência robusta |
| Java 21 + Spring Boot 4.x | Spring Boot 3.5.x | Suporte OSS ativo no início do projeto |