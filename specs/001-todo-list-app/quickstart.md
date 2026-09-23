# Quickstart — validação ponta a ponta

**Data**: 2026-09-11
**Escopo**: Como rodar e validar o To-Do List App ponta a ponta, sem
implementar código. Consulte [data-model.md](./data-model.md) e
[contracts/api-rest.md](./contracts/api-rest.md) para detalhes de modelo e
contrato.

## Pré-requisitos

- Docker + Docker Compose instalados e com daemon ativo.
- Navegador web moderno (Chrome/Firefox/Edge) e Node.js ≥ 20 para a suíte E2E.

## Subir a aplicação

```bash
docker compose up --build -d
docker compose ps        # frontend (nginx) e backend (api) ambos healthy
```

Porta padrão: `http://localhost:8080` (nginx serve estáticos e faz proxy de
`/api/*`). A base SQLite é criada automaticamente no volume nomeado.

## Cenários de validação (manuais ou via Playwright)

| # | Cenário | Passos | Resultado esperado |
|---|---------|--------|--------------------|
| 1 | Criar tarefa | Digitar "Comprar leite" + descrição "2 litros" e salvar | Aparece na lista como "Pendente", título e descrição corretos |
| 2 | Criar sem título | Deixar título vazio (ou só espaços) e salvar | Mensagem "Título é obrigatório." e tarefa não criada |
| 3 | Título excedente | Digitar > 120 caracteres e salvar | Mensagem de erro clara; tarefa não criada |
| 4 | Marcar concluída | Clicar no checkbox de uma tarefa pendente | Status muda para "Concluída" instantaneamente (visual < 1 s) |
| 5 | Editar | Alterar título e/ou descrição | Valores atualizados refletidos na lista |
| 6 | Excluir | Remover uma tarefa | Some da lista |
| 7 | Persistência | Criar/editaren/marcar algumas tarefas e recarregar a página (`F5`) | Tudo permanece com dados e status corretos |
| 8 | Ordenação | Criar tarefas em status mistos | Pendentes antes de concluídas; mais recentes primeiro em cada grupo |
| 9 | Lista vazia | Excluir todas as tarefas | Mensagem amigável de lista vazia |

## Verificação direta da API (alternativa aos cenários de UI)

```bash
curl -s http://localhost:8080/api/tasks                       # lista
curl -s -X POST http://localhost:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Comprar leite","description":"2 litros"}'     # cria (201)
curl -s -i -X POST http://localhost:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"  "}'                                        # 400 — título obrigatório
curl -s -X PUT http://localhost:8080/api/tasks/1 \
  -H 'Content-Type: application/json' \
  -d '{"status":"completed"}'                                 # conclui
curl -s -i -X DELETE http://localhost:8080/api/tasks/1        # exclui (204)
```

## Testes automatizados

### Back-end (JUnit + MockMvc)

```bash
cd backend && mvn test
```

Esperado: suíte verde cobrindo todos os endpoints (criar/listar/visualizar/
atualizar/excluir) e regras de negócio (título obrigatório, limite 120, status
inválido, 404).

### E2E full-stack (Playwright)

```bash
cd e2e && npm ci && npx playwright test
```

Cenários cobertos no [quickstart acima](#cenários-de-validação): CRUD,
persistência pós-recarga e ordenação, rodando contra a stack em Docker.
Evidências (traces) em `e2e/test-results/`.

## Critérios de aceite da validação

Todos os cenários 1–9 passam; back-end e E2E verdes. A aplicação permanece
responsiva com 100 tarefas (FR-012) e as operações respondem conforme os
critérios SC-001 a SC-007.