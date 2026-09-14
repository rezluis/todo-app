# Contrato da API REST

**Base path**: `/api/tasks`
**Formato**: JSON (`application/json`)
**Acesso**: same-origin via nginx proxy (sem CORS). Sem autenticação (uso
pessoal, um usuário).

## Representação da tarefa

```json
{
  "id": 1,
  "title": "Comprar leite",
  "description": "2 litros",
  "status": "pending",
  "createdAt": 1726063200000,
  "updatedAt": 1726063200000
}
```

| Campo | Tipo | Obrigatório (payload) | Regras |
|-------|------|-----------------------|--------|
| `id` | long | — (server) | gerado pelo banco |
| `title` | string | sim | vazio/espacos → "Sem título"; max 120 |
| `description` | string | não | ausência tratada como sem descrição |
| `status` | string | sim (na criação) | `pending` / `completed` |
| `createdAt` | long | — (server) | epoch ms |
| `updatedAt` | long | — (server) | epoch ms |

## Endpoints

### `GET /api/tasks` — listar tarefas

Resposta `200 OK` com array de tarefas na ordenação determinística (pendentes
primeiro; dentro do grupo, mais recentes primeiro — `created_at DESC, id DESC`).

### `POST /api/tasks` — criar tarefa

Corpo (aplica-se edge case do título):

```json
{ "title": "Comprar leite", "description": "2 litros" }
```

- `201 Created` — corpo com a tarefa criada (incl. `id`, `createdAt`,
  `updatedAt`; `status` assume `pending`).
- `400 Bad Request` — título > 120 caracteres, `status` inválido no corpo ou
  payload malformado. Body de erro: `{ "message": "..." }`.

### `GET /api/tasks/{id}` — visualizar tarefa

- `200 OK` — tarefa encontrada.
- `404 Not Found` — `{ "message": "..." }`.

### `PUT /api/tasks/{id}` — atualizar tarefa

Corpo (campos opcionais; apenas os presentes são alterados):

```json
{ "title": "Comprar leite e pão", "description": "1L desnatado", "status": "completed" }
```

- `200 OK` — tarefa atualizada (com novos `updatedAt`).
- Título vazio/espaços no body → armazenado "Sem título" (FR-007).
- `400 Bad Request` — título > 120 caracteres ou status inválido.
- `404 Not Found` — id inexistente.

### `DELETE /api/tasks/{id}` — excluir tarefa

- `204 No Content` — excluída com sucesso.
- `404 Not Found` — id inexistente (front-end trata sem erro visível ao
  usuário; exclusão dupla é sem efeito no lado da UI).

## Formato de erro

```json
{ "message": "Título excede o limite de 120 caracteres." }
```

Códigos utilizados: `400` (validação/payload), `404` (recurso inexistente),
`500` (falha interna de gravação — FR-013).

## Casos de borda no contrato

- Criar/atualizar com `title` ausente, `null`, `""` ou `"   "` → salvo como
  `"Sem título"` (FR-001/002/007) — **não** gera 400.
- `status` inválido NUNCA é aceito silenciosamente (400).
- Só espaços não equivalem a título vazio quando classificam como texto; a
  regra vale para o valor final do campo.