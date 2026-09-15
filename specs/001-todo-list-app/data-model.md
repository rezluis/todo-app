# Data Model: To-Do List App

**Data**: 2026-09-11 | **Fonte**: [spec.md](./spec.md) | **Backend**: SQLite

## Entidade: Task (Tarefa)

Tabela única `tasks`. Representa um item da lista de tarefas.

| Campo | Tipo SQL | Restrições | Regras derivadas do spec |
|-------|----------|------------|--------------------------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Identificador único interno. |
| `title` | TEXT | NOT NULL, ≤ 120 caracteres | FR-001/FR-002: obrigatório; vazio/só espaços → rejeitado (400) no salvamento. |
| `description` | TEXT | NULL | Opcional (FR-001); sem limite rígido, truncada visualmente na UI. |
| `status` | TEXT | NOT NULL, DEFAULT 'pending', CHECK in ('pending','completed') | FR-005: alternância entre pendente/concluída. |
| `created_at` | INTEGER | NOT NULL | Timestamp (epoch ms) de criação — base da ordenação "mais recente primeiro". |
| `updated_at` | INTEGER | NOT NULL | Timestamp (epoch ms) da última atualização. |

### Índices

- `idx_tasks_status_created` em `(status, created_at DESC)` — consulta da
  lista ordenada (FR-014) sem sort em memória.

## Regras de validação (origem nos requisitos)

- **Título (FR-001, FR-002, FR-007, FR-010)**: normalização "back-side".
  Título nulo, vazio (`""`) ou somente espaços → rejeitado (HTTP 400) com a
  mensagem "Título é obrigatório." no create e no update (quando enviado no
  payload). Em `update`, `title` ausente no payload mantém o título atual.
  Título com mais de 120 caracteres → rejeitado (HTTP 400), mensagem clara
  (FR-010).
- **Descrição (FR-001)**: opcional; se nula no payload, tratada como ausente.
- **Status (FR-005)**: somente `pending` / `completed`; valor inválido →
  HTTP 400.

### Transições de estado

```text
pending ⇄ completed   (toggle, FR-005 — via PUT status)
```

Sem estados intermediários. Nenhuma restrição temporal de transição.

## Ordenação (FR-014)

Consulta da lista: `ORDER BY` status primeiro (pendentes antes de concluídas),
depois `created_at DESC, id DESC` dentro de cada grupo. O desempate por `id`
garante ordem estritamente determinística para criações no mesmo instante.

## Relacionamentos

- `Task` é autônoma — sem relacionamentos, sem chaves estrangeiras (um único
  usuário, sem dono de tarefa).

## Persistência

- Arquivo único SQLite em volume nomeado Docker (`/data/todo.db`).
- `schema.sql` idempotente (`CREATE TABLE IF NOT EXISTS`) executado em todo
  boot (`spring.sql.init.mode=always`).
- `PRAGMA journal_mode=WAL`, `PRAGMA synchronous=NORMAL` e
  `PRAGMA foreign_keys=ON` aplicados por conexão.