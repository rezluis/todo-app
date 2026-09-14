# Todo-App

Aplicação de lista de tarefas (To-Do List) desacoplada e simples:

- **Front-end**: JavaScript Vanilla + HTML + CSS (sem frameworks), servido por
  nginx e consumindo a API via `fetch`.
- **Back-end**: API RESTful com Spring Boot (Java 21).
- **Banco de dados**: SQLite (arquivo único, embarcado, sem servidor).
- **Orquestração**: Docker Compose; nginx faz proxy reverso de `/api/*` para o
  back-end (same-origin, sem CORS).

Mais detalhes em `specs/001-todo-list-app/` (spec, plan, contratos, modelo de
dados e quickstart de validação).

## Como subir

```bash
docker compose up --build -d
```

Acesse `http://localhost:8080`. O banco SQLite fica em um volume nomeado
(`todo-data`) e sobrevive a `up/down` e recomposições.

## Como rodar os testes

Back-end (JUnit + MockMvc):

```bash
cd backend && mvn test
```

E2E full-stack (Playwright — requer a aplicação rodando em `http://localhost:8080`):

```bash
cd e2e && npm ci && npx playwright test
```

Persistência após reinício do backend:

```bash
e2e/scripts/check-restart.sh
```

## Estrutura

```text
compose.yml        Orquestração (frontend + backend + volume)
backend/           API REST Spring Boot + SQLite + testes MockMvc
frontend/          Estáticos Vanilla JS (nginx + proxy reverso)
e2e/               Testes Playwright (CRUD, persistência, ordenação, escala)
specs/             Documentação Spec Kit (constitution, spec, plan, contratos)
```