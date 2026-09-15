# Implementation Plan: To-Do List App

**Branch**: `001-todo-list-app` | **Date**: 2026-09-11 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-todo-list-app/spec.md`

## Summary

Aplicação web desacoplada de lista de tarefas para uso pessoal (um único
usuário, sem autenticação). Front-end em JavaScript Vanilla/HTML/CSS consome
uma API RESTful exposta por um back-end Spring Boot (Java). Os dados são
persistidos em SQLite (arquivo único, embarcado) e a solução é orquestrada com
Docker Compose; um contêiner nginx serve o front-end estático e faz proxy do
caminho `/api/*` para o back-end (same-origin, eliminando CORS). CRUD completo
(título obrigatório, descrição opcional, status
pendente/concluída), ordenação determinística (pendentes primeiro; mais
recentes primeiro dentro do grupo) e persistência garantida por volume nomeado.
Testes automatizados obrigatórios: JUnit/MockMvc no back-end e Playwright
(E2E) cobrindo os fluxos de usuário e a persistência pós-recarga.

## Technical Context

**Language/Version**: Java 21 (LTS) para o back-end; HTML5/CSS3/JavaScript
(Vanilla, ES2020+) para o front-end.

**Primary Dependencies**:
- Back-end: `spring-boot-starter-web`, `spring-boot-starter-jdbc`,
  `org.xerial:sqlite-jdbc` (driver embedded); escopo dev/test:
  `spring-boot-starter-test`.
- Front-end: nenhuma dependência de runtime (Vanilla JS, `fetch` API).
- Infra: `nginx:alpine` (estático + proxy reverso), Docker Compose.
- E2E: Playwright.

**Storage**: SQLite — arquivo único (ex.: `todo.db`) em volume nomeado Docker;
inicialização do schema via `schema.sql` idempotente; `PRAGMA journal_mode=WAL`,
`PRAGMA foreign_keys=ON` e `PRAGMA synchronous=NORMAL` por conexão.

**Testing**: JUnit 5 + Spring Boot Test (MockMvc + JsonPath) no back-end;
Playwright (E2E full-stack) para o front-end e fluxos de usuário.

**Target Platform**: Contêineres Docker (Linux, x86_64/arm64); navegadores web
modernos (Chrome/Firefox/Safari/Edge).

**Project Type**: Web application — front-end estático + API REST de serviço.

**Performance Goals**: Resposta da API < 200 ms p95; feedback visual da ação
"marcar concluída" < 1 s (SC-002).

**Constraints**: Front-end SEM frameworks (constitucional); banco de dados leve
e embarcado, sem servidor separado (constitucional); título com limite de 120
caracteres (FR-010); operação responsiva com 100 tarefas (FR-012); sem
autenticação/multi-usuário (clarificado); ordenação pendentes-primeiro
(FR-014).

**Scale/Scope**: 1 usuário, até 100 tarefas, single-node via Docker Compose.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Requisito constitucional | Status |
|------|--------------------------|--------|
| G1 | I. Simplicidade — dependências mínimas e justificadas; YAGNI | PASS (deps mínimas, sem JPA/Lombok; justificativa na tabela abaixo) |
| G2 | II. Código Limpo — nomes significativos, responsabilidade única, sem duplicação | PASS (estrutura pequena; regras aplicadas na implementação) |
| G3 | III. Testes Automatizados (NON-NEGOTIABLE) — todo comportamento testado automaticamente | PASS (JUnit/MockMvc + Playwright E2E) |
| G4 | IV. Arquitetura Desacoplada — front-end Vanilla JS consome API RESTful; sem UI servida pelo back-end | PASS (contrato REST/JSON; nginx proxy; front-end sem acesso a dados) |
| G5 | V. Banco de Dados Leve — embarcado, fácil configuração, reproduzível | PASS (SQLite arquivo único + schema.sql + volume) |

Re-check pós-design: sem violações introduzidas pelos artefatos da Phase 1.

## Project Structure

### Documentation (this feature)

```text
specs/[001-todo-list-app]/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
│   └── api-rest.md
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
compose.yml                 # Orquestração: frontend + backend + volume (Docker Compose)

backend/                    # API REST — Spring Boot (Java 21)
├── Dockerfile              # Build multi-stage (Maven → JRE slim, usuário não-root)
├── pom.xml                 # spring-boot-starter-web, starter-jdbc, sqlite-jdbc, starter-test
└── src/
    ├── main/
    │   ├── java/com/todo/
    │   │   ├── TodoApplication.java
    │   │   ├── api/TaskController.java       # endpoints REST
    │   │   ├── domain/Task.java              # modelo/record da tarefa
    │   │   ├── service/TaskService.java      # regras de negócio (título obrigatório, validação)
    │   │   └── repo/TaskRepository.java      # acesso a dados (JdbcTemplate)
    │   └── resources/
    │       ├── application.properties        # datasource SQLite, init mode=always
    │       └── schema.sql                    # CREATE TABLE IF NOT EXISTS + índices
    └── test/java/com/todo/
        └── api/
            ├── TaskControllerTest.java       # MockMvc + JsonPath (contrato)
            └── TaskServiceTest.java          # regras: título padrão, limites, status

frontend/                   # App estática — Vanilla JS + HTML + CSS (sem frameworks)
├── Dockerfile              # nginx:alpine servindo estáticos
├── nginx.conf              # estáticos + proxy reverso /api/* → backend
├── index.html
├── css/styles.css
└── js/
    ├── api.js              # wrappers fetch p/ a API REST
    └── app.js              # renderização da lista + eventos (CRUD, toggle)

e2e/                        # Testes de ponta a ponta (Playwright)
├── package.json
├── playwright.config.js
└── tests/
    ├── todo-crud.spec.js   # criar, editar, excluir, marcar concluída
    └── todo-persistence.spec.js  # persistência pós-recarga + ordenação
```

**Structure Decision**: Adotada a estrutura "web application" (front-end +
back-end) do template. Camadas no back-end seguem a separação
controller/service/repository recomendada pela pesquisa e por convenção; o
front-end mantém apenas dois módulos JS (API + UI) para evitar
multiplicidade de arquivos sem ganho (YAGNI).

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Framework Spring Boot no back-end (dependência mais pesada que o mínimo) | Stack explicitamente determinada pelo produto (requisição do usuário); fornece REST/JSON, testes MockMvc e configuração declarativa | Servidor HTTP Java embutido puro ou Node.js seriam menores, mas contradizem a decisão de stack declarada; a opção intermediária (JPA/Hibernate) foi rejeitada para manter G1 — usa-se JdbcTemplate |
| Dois contêineres (nginx + backend) em vez de contêiner único | Separação de responsabilidades do front-end estático e da API (G4); nginx também faz proxy reverso /api| Contêiner único misturaria web server e serviço, acoplando UI e API e dificultando testes isolados |