---

description: "Task list template for feature implementation"

---

# Tasks: To-Do List App

**Input**: Design documents from `/specs/001-todo-list-app/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/api-rest.md, quickstart.md

**Tests**: A constituição do projeto (Princípio III — Testes Automatizados,
NON-NEGOTIABLE) exige testes automatizados para TODO comportamento. Portanto,
todas as fases de user story incluem tarefas de teste obrigatórias, escritas
ANTES da implementação (espera-se que falhem) e executadas em CI/validação.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Web app**: `backend/src/`, `frontend/`, `e2e/` (estrutura definida no plan.md)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Create source tree per plan.md: `backend/`, `frontend/`, `e2e/`, `compose.yml` na raiz do repositório
- [x] T002 Initialize Spring Boot project: `backend/pom.xml` com `spring-boot-starter-parent`, `spring-boot-starter-web`, `spring-boot-starter-jdbc`, `org.xerial:sqlite-jdbc`, `spring-boot-starter-test`; criar `backend/src/main/java/com/todo/TodoApplication.java`
- [x] T003 [P] Create container scaffolding: `backend/Dockerfile` (multi-stage temurin 21 jdk→jre, usuário não-root), `frontend/Dockerfile` (nginx:alpine), `frontend/nginx.conf` (servir estáticos + proxy `/api/*` → backend para eliminar CORS), `compose.yml` (serviços frontend/backend, porta 8080→nginx, volume nomeado `todo-data` montado em `/data`, `restart: unless-stopped`, healthcheck do backend)
- [x] T004 [P] Initialize Playwright e2e project: `e2e/package.json`, `e2e/playwright.config.js` (baseURL `http://localhost:8080`, browsers chromium), diretório `e2e/tests/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T005 Create `backend/src/main/resources/schema.sql` com tabela `tasks` idempotente (`CREATE TABLE IF NOT EXISTS`) seguindo data-model.md: colunas `id` INTEGER PRIMARY KEY AUTOINCREMENT, `title` TEXT NOT NULL, `description` TEXT NULL, `status` TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','completed')), `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL; índice `idx_tasks_status_created` em `(status, created_at DESC)`
- [x] T006 Configure `backend/src/main/resources/application.properties`: datasource `spring.datasource.url=jdbc:sqlite:${DB_PATH:/data/todo.db}` via env, `spring.datasource.driver-class-name=org.sqlite.JDBC`, `spring.sql.init.mode=always`, e `spring.datasource.hikari.connection-init-sql` executando `PRAGMA journal_mode=WAL; PRAGMA synchronous=NORMAL; PRAGMA foreign_keys=ON;`
- [x] T007 Create `Task` record em `backend/src/main/java/com/todo/domain/Task.java` com campos `id`,`title`,`description`,`status`,`createdAt`,`updatedAt` e mapeamento JSON camelCase↔colunas snake_case conforme data-model.md
- [x] T008 Create `TaskRepository` em `backend/src/main/java/com/todo/repo/TaskRepository.java` com `JdbcTemplate`: `insert` (retorna id), `findAll` (ordenação `ORDER BY (status='completed'), created_at DESC, id DESC` — pendentes antes de concluídas, mais recentes primeiro, desempate por id), `findById`, `update`, `delete`
- [x] T009 [P] Create `RestExceptionHandler` em `backend/src/main/java/com/todo/api/RestExceptionHandler.java` retornando corpo de erro `{ "message": "..." }` para 400 (validação: título > 120 caracteres, status inválido, payload malformado), 404 (recurso inexistente) e 500, conforme contracts/api-rest.md
- [x] T010 Create test infrastructure do backend: config base `@SpringBootTest` reutilizada por `backend/src/test/java/com/todo/api/TaskControllerTest.java` e `backend/src/test/java/com/todo/service/TaskServiceTest.java` (MockMvc + JsonPath)

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Criar tarefa (Priority: P1) 🎯 MVP

**Goal**: O usuário cria uma tarefa (título + descrição opcional) e ela aparece na lista. Título vazio/só espaços → "Sem título" (FR-001/FR-002).

**Independent Test**: Criar tarefa com e sem título via UI ou `curl POST /api/tasks`; verificar resposta 201 e presença na lista.

### Tests for User Story 1 (OBRIGATÓRIOS - constituição Princípio III) ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T011 [P] [US1] Teste de serviço em `backend/src/test/java/com/todo/service/TaskServiceTest.java`: create com título preenchido, vazio, nulo e só espaços → grava "Sem título"; status default `pending`; `createdAt`/`updatedAt` preenchidos; título > 120 caracteres → erro 400
- [x] T012 [P] [US1] Teste de contrato em `backend/src/test/java/com/todo/api/TaskControllerTest.java`: `POST /api/tasks` retorna 201 com JSON conforme contracts/api-rest.md (id, title, description, status, createdAt, updatedAt)

### Implementation for User Story 1

- [x] T013 [US1] Implement `TaskService.create` em `backend/src/main/java/com/todo/service/TaskService.java`: normaliza título (vazio/nulo/só espaços → "Sem título"), valida limite de 120 caracteres, default status `pending`, preenche `createdAt`/`updatedAt` (epoch ms)
- [x] T014 [US1] Implement `POST /api/tasks` em `backend/src/main/java/com/todo/api/TaskController.java` (201 + corpo da tarefa criada; 400 em validação)
- [x] T015 [P] [US1] Implement `api.js` `createTask` em `frontend/js/api.js` (fetch POST `/api/tasks`, JSON, tratamento de erro) 
- [x] T016 [P] [US1] Implement formulário de criação inline em `frontend/index.html` + estilo em `frontend/css/styles.css` (campo título, campo descrição, botão adicionar)
- [x] T017 [US1] Implement handler de submit do formulário em `frontend/js/app.js` (chama `createTask`, limpa o campo, renderiza a nova tarefa na lista)
- [x] T018 [US1] E2E cenário "Criar tarefa" com e sem título em `e2e/tests/todo-crud.spec.js` (Playwright, visando `http://localhost:8080`)

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Visualizar tarefas (Priority: P1)

**Goal**: O usuário vê todas as tarefas (título, descrição, status) e o estado vazio amigável (FR-003/FR-004).

**Independent Test**: Abrir a aplicação e ver a lista completa; com lista vazia, ver mensagem amigável.

### Tests for User Story 2 (OBRIGATÓRIOS - constituição Princípio III) ⚠️

- [x] T019 [P] [US2] Teste de contrato em `backend/src/test/java/com/todo/api/TaskControllerTest.java`: `GET /api/tasks` retorna 200 com array ordenado (pendentes primeiro, depois concluídas, mais recentes primeiro); lista vazia → `[]`
- [x] T020 [P] [US2] Teste de serviço em `backend/src/test/java/com/todo/service/TaskServiceTest.java`: `findAll` retorna ordenação determinística (status, `created_at DESC`, `id DESC`) e 0 tarefas → lista vazia

### Implementation for User Story 2

- [x] T021 [US2] Implement `TaskService.findAll` em `backend/src/main/java/com/todo/service/TaskService.java` (delega ao repositório; sem ordenação em memória)
- [x] T022 [US2] Implement `GET /api/tasks` em `backend/src/main/java/com/todo/api/TaskController.java`
- [x] T023 [P] [US2] Implement render da lista (título, descrição, status distinguível) e estado vazio amigável em `frontend/js/app.js` + `frontend/index.html`
- [x] T024 [US2] E2E cenário "Visualizar tarefas" e "Lista vazia" em `e2e/tests/todo-crud.spec.js`

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Marcar concluída com 1 clique (Priority: P1)

**Goal**: Alternância `pending ⇄ completed` com um único clique, feedback visual < 1 s (FR-005, SC-002).

**Independent Test**: Clicar no controle de uma tarefa e ver o status mudar imediatamente; estado persiste.

### Tests for User Story 3 (OBRIGATÓRIOS - constituição Princípio III) ⚠️

- [x] T025 [P] [US3] Teste de contrato em `backend/src/test/java/com/todo/api/TaskControllerTest.java`: `PUT /api/tasks/{id}` com `status` `pending`→`completed` e volta retorna 200; status inválido → 400; id inexistente → 404
- [x] T026 [P] [US3] Teste de serviço em `backend/src/test/java/com/todo/service/TaskServiceTest.java`: toggle de status atualiza `updatedAt` e mantém demais campos

### Implementation for User Story 3

- [x] T027 [US3] Implement suporte a atualização parcial em `TaskService.update` em `backend/src/main/java/com/todo/service/TaskService.java` (alterar apenas `status` quando solicitado; atualiza `updatedAt`)
- [x] T028 [US3] Implement `PUT /api/tasks/{id}` em `backend/src/main/java/com/todo/api/TaskController.java`
- [x] T029 [P] [US3] Implement checkbox/clique único de conclusão em `frontend/js/app.js` + `frontend/index.html` (feedback visual imediato/otimista, sem recarregar a página)
- [x] T030 [US3] E2E cenário "Marcar concluída com 1 clique" e retorno ao estado pendente em `e2e/tests/todo-crud.spec.js`

**Checkpoint**: User Story 3 working independently

---

## Phase 6: User Story 4 - Editar tarefa (Priority: P1)

**Goal**: Edição de título e/ou descrição com salvamento; título vazio na edição → "Sem título" (FR-006/FR-007/FR-010).

**Independent Test**: Editar título/descrição e ver valores atualizados na lista; editar para branco → "Sem título".

### Tests for User Story 4 (OBRIGATÓRIOS - constituição Princípio III) ⚠️

- [x] T031 [P] [US4] Teste de contrato em `backend/src/test/java/com/todo/api/TaskControllerTest.java`: `PUT /api/tasks/{id}` atualiza título+descrição → 200; título em branco/só espaços → salvo "Sem título"; título > 120 → 400; id inexistente → 404
- [x] T032 [P] [US4] Teste de serviço em `backend/src/test/java/com/todo/service/TaskServiceTest.java`: edição preserva `id` e `createdAt`, atualiza `updatedAt`, mantém campos não enviados

### Implementation for User Story 4

- [x] T033 [US4] Completar `TaskService.update` em `backend/src/main/java/com/todo/service/TaskService.java` para título/descrição (normalização "Sem título" na edição; validação 120; campos ausentes não alterados)
- [x] T034 [P] [US4] Implement UI de edição (formulário inline/expansível) em `frontend/index.html` + `frontend/js/app.js` + `frontend/css/styles.css`
- [x] T035 [US4] Wire do submit de edição → `api.js` `updateTask` (fetch PUT) e rerender em `frontend/js/app.js`
- [x] T036 [US4] E2E cenário "Editar tarefa" em `e2e/tests/todo-crud.spec.js`

**Checkpoint**: User Story 4 working independently

---

## Phase 7: User Story 5 - Excluir tarefa (Priority: P1)

**Goal**: Exclusão com remoção da lista; exclusão dupla sem erro visível (FR-008; edge case).

**Independent Test**: Excluir uma tarefa e ver a lista atualizar; excluir a última → lista vazia.

### Tests for User Story 5 (OBRIGATÓRIOS - constituição Princípio III) ⚠️

- [x] T037 [P] [US5] Teste de contrato em `backend/src/test/java/com/todo/api/TaskControllerTest.java`: `DELETE /api/tasks/{id}` → 204; id inexistente → 404
- [x] T038 [P] [US5] Teste de serviço em `backend/src/test/java/com/todo/service/TaskServiceTest.java`: delete remove a tarefa; comportamento para id inexistente definido (404)

### Implementation for User Story 5

- [x] T039 [US5] Implement `TaskService.delete` e `DELETE /api/tasks/{id}` em `backend/src/main/java/com/todo/service/TaskService.java` + `backend/src/main/java/com/todo/api/TaskController.java`
- [x] T040 [P] [US5] Implement botão excluir + handler em `frontend/js/app.js` + `frontend/index.html` (exclusão dupla tratada sem erro visível; remove da lista; última → estado vazio)
- [x] T041 [US5] E2E cenário "Excluir tarefa" e "Lista vazia após excluir última" em `e2e/tests/todo-crud.spec.js`

**Checkpoint**: User Story 5 working independently

---

## Phase 8: User Story 6 - Persistir dados entre recarregamentos (Priority: P1)

**Goal**: Tarefas sobrevivem a recarga/reabertura (FR-009); dados no volume nomeado sobrevivem a reinício de contêiner.

**Independent Test**: Criar/editaren/marcar tarefas, recarregar (F5) e reiniciar contêineres; verificar que nada se perdeu.

### Tests for User Story 6 (OBRIGATÓRIOS - constituição Princípio III) ⚠️

- [x] T042 [P] [US6] E2E cenário "Persistência pós-recarga" em `e2e/tests/todo-persistence.spec.js`: criar, editar, marcar concluída e recarregar a página → todas as tarefas permanecem com título, descrição e status corretos
- [x] T043 [US6] Validação de persistência em reinício: script/cenário que executa `docker compose restart backend` (ou down/up sem `-v`) e confere que os dados continuam presentes (volume nomeado `todo-data` em `/data`, schema.sql idempotente não recria dados)

### Implementation for User Story 6

- [x] T044 [US6] Confirmar/configurar `frontend/js/app.js` + `api.js` para carregar a lista via `GET /api/tasks` a cada abertura/recarga (sem cache obsolescente) e validar volume/SQLite no `compose.yml` — ajustar se necessário

**Checkpoint**: User Story 6 working independently

---

## Phase 9: User Story 7 - Ordenação da lista (Priority: P2)

**Goal**: Ordem determinística: pendentes primeiro; dentro do grupo, mais recentes primeiro (FR-014, SC-007).

**Independent Test**: Criar tarefas em status mistos e verificar a ordem exibida e mantida após recarga.

### Tests for User Story 7 (OBRIGATÓRIOS - constituição Princípio III) ⚠️

- [x] T045 [P] [US7] Teste de serviço/repositório em `backend/src/test/java/com/todo/service/TaskServiceTest.java`: lista ordenada com pendentes antes de concluídas e, em cada grupo, `created_at DESC, id DESC` (empate de timestamp → desempate por id)
- [x] T046 [P] [US7] E2E cenário "Ordenação" em `e2e/tests/todo-persistence.spec.js`: status mistos → pendentes primeiro; ordem preservada após recarga

### Implementation for User Story 7

- [x] T047 [US7] Garantir `ORDER BY` de `TaskRepository.findAll` em `backend/src/main/java/com/todo/repo/TaskRepository.java` (sem ordenação no front-end) e revisar se algum cenário de UI depende de ordem do servidor — ajustar `frontend/js/app.js` se necessário

**Checkpoint**: All user stories should now be independently functional

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [x] T048 Run suíte de testes do back-end: `cd backend && mvn test` → verde
- [x] T049 Run suíte E2E full-stack: `cd e2e && npm ci && npx playwright test` → verde
- [x] T050 [P] Validar responsividade com 100 tarefas (FR-012, SC-005): cenário Playwright ou script de seed criando 100 tarefas e verificando operações funcionais e tela legível
- [x] T051 [P] Documentação: `README.md` na raiz com setup `docker compose up --build`, portas e link para `specs/001-todo-list-app/quickstart.md`
- [x] T052 Code cleanup e passada de conformidade com a constituição (G1–G5): simplicidade de deps, clean code, testes cobrindo comportamentos, desacoplamento via contrato REST, SQLite leve
- [x] T053 Validação final pelo `quickstart.md`: executar os 9 cenários + verificações `curl` da API (201/200/204/404/400) e conferir critérios SC-001 a SC-007

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational (Phase 2) - Independently testable (reusa `TaskRepository` apenas via serviço)
- **User Story 3 (P1)**: Can start after Foundational (Phase 2) - Reusa endpoint `PUT`; independente das demais
- **User Story 4 (P1)**: Can start after Foundational (Phase 2) - Compartilha o `PUT /api/tasks/{id}` e a UI de edição; independente das demais
- **User Story 5 (P1)**: Can start after Foundational (Phase 2) - Independente
- **User Story 6 (P1)**: Requer US1–US5 implementadas para validar persistência real de todos os estados; cenários E2E dependem de API e UI prontas
- **User Story 7 (P2)**: Requer US2 (listagem) para validar a ordenação exibida
- **Polish (Final)**: Depends on US1–US7 complete

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Services before endpoints
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel (T003, T004)
- All Foundational tasks marked [P] can run in parallel (T009; demais em sequência por compartilharem schema/contexto) 
- Once Foundational phase completes, US1–US5 can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Front-end tasks ([P]) e back-end tasks podem ser paralelizados dentro de uma mesma story
- US6 e US7 (fases 8 e 9) podem ser executadas em paralelo após US1–US5

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Teste de serviço create (título padrão, limite 120) em TaskServiceTest.java"
Task: "Teste de contrato POST /api/tasks em TaskControllerTest.java"

# Launch back-end and front-end implementation tasks in parallel:
Task: "Implement TaskService.create em backend/src/main/java/com/todo/service/TaskService.java"
Task: "Implement api.js createTask em frontend/js/api.js"
Task: "Implement formulário de criação em frontend/index.html + frontend/css/styles.css"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Add User Story 4 → Test independently → Deploy/Demo
6. Add User Story 5 → Test independently → Deploy/Demo
7. Add User Story 6 (persistência) → validação pós-recarga/reinício
8. Add User Story 7 (ordenação) → validação determinística
9. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Stories 1–2 (back-end + front-end)
   - Developer B: User Stories 3–5 (UI + API)
   - Developer C: User Stories 6–7 (E2E + validação)
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Cada user story deve ser completável e testável de forma independente
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
- Testes são OBRIGATÓRIOS em todas as user stories (constituição, Princípio III); E2E roda contra a stack em Docker (`docker compose up --build`)