# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e o
versionamento segue [SemVer](https://semver.org/lang/pt-BR/).

## [Unreleased]

### Added

- **Filtro por status na listagem** (issue #8) — manutenção perfectiva
  (ISO/IEC 14764): `GET /api/tasks` aceita o query param opcional `status`
  com valores `TODO`, `IN_PROGRESS` e `DONE`. Ausente retorna todas as
  tarefas; valor inválido retorna HTTP 400 com `{ "message": "..." }`. A
  comparação é case-sensitive e sem normalização de espaços (`todo` e
  `" DONE "` são rejeitados); com múltiplos valores
  (`?status=TODO&status=DONE`), usa-se apenas o primeiro. Escopo backend-only:
  a interface não ganhou controle de filtro.

### Changed

- **Título obrigatório** (issue #1) — manutenção perfectiva (ISO/IEC 14764):
  o fallback que salvava tarefas sem título como "Sem título" foi removido.
  Criar ou editar uma tarefa com título vazio, nulo ou apenas espaços agora é
  rejeitado com HTTP 400 e mensagem `"Título é obrigatório."`; a interface
  exibe a mesma mensagem sem enviar a requisição. A regra vale apenas para
  novos salvamentos — tarefas já existentes com "Sem título" não são alteradas.
- **Domínio de status migrado** (issue #8) — manutenção perfectiva
  (ISO/IEC 14764): os estados `pending`/`completed` foram substituídos por
  `TODO`, `IN_PROGRESS` e `DONE` em todo o sistema (schema, API REST,
  front-end e testes). Bases existentes migram via
  `backend/src/main/resources/migrations/V2__status_domain_todo_inprogress_done.sql`
  (rebuild da tabela, mapeamento `pending`→`TODO` e `completed`→`DONE`,
  idempotente). A ordenação da lista agora coloca tarefas não-`DONE` antes de
  `DONE`, com as mais recentes primeiro em cada grupo.
