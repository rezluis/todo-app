-- V2: domínio de status 'pending'/'completed' -> 'TODO'/'IN_PROGRESS'/'DONE'
-- Mapeamento: pending -> TODO, completed -> DONE (IN_PROGRESS ainda não ocorre em dados existentes).
--
-- O SQLite não altera CHECK constraints via ALTER TABLE; a tabela precisa ser
-- recriada (rebuild) com os dados copiados. Idempotente: pode ser reexecutado.
--
-- Aplicação (única vez, em bases existentes):
--   sqlite3 /data/todo.db < V2__status_domain_todo_inprogress_done.sql
-- Instalações novas não precisam deste script (schema.sql já cria com o novo domínio).

PRAGMA foreign_keys=OFF;

BEGIN;

DROP TABLE IF EXISTS tasks_v2;

CREATE TABLE tasks_v2 (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    status TEXT NOT NULL DEFAULT 'TODO' CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

INSERT INTO tasks_v2 (id, title, description, status, created_at, updated_at)
SELECT id, title, description,
       CASE status
           WHEN 'pending' THEN 'TODO'
           WHEN 'completed' THEN 'DONE'
           ELSE status
       END,
       created_at,
       updated_at
FROM tasks;

DROP TABLE tasks;

ALTER TABLE tasks_v2 RENAME TO tasks;

CREATE INDEX IF NOT EXISTS idx_tasks_status_created ON tasks (status, created_at DESC);

COMMIT;

PRAGMA foreign_keys=ON;
