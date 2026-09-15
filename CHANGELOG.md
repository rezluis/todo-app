# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e o
versionamento segue [SemVer](https://semver.org/lang/pt-BR/).

## [Unreleased]

### Changed

- **Título obrigatório** (issue #1) — manutenção perfectiva (ISO/IEC 14764):
  o fallback que salvava tarefas sem título como "Sem título" foi removido.
  Criar ou editar uma tarefa com título vazio, nulo ou apenas espaços agora é
  rejeitado com HTTP 400 e mensagem `"Título é obrigatório."`; a interface
  exibe a mesma mensagem sem enviar a requisição. A regra vale apenas para
  novos salvamentos — tarefas já existentes com "Sem título" não são alteradas.