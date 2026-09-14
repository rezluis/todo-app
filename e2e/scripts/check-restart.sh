#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/../.."

BASE=${BASE_URL:-http://localhost:8080}
TITLE="persistencia-$(date +%s)"

echo "Criando tarefa de persistência: $TITLE"
curl -sf -X POST "$BASE/api/tasks" \
  -H 'Content-Type: application/json' \
  -d "{\"title\":\"$TITLE\"}" >/dev/null

echo "Reiniciando contêiner backend..."
docker compose restart backend

echo "Aguardando API voltar..."
until curl -sf "$BASE/api/tasks" >/dev/null 2>&1; do
  sleep 1
done

if curl -sf "$BASE/api/tasks" | grep -q "$TITLE"; then
  echo "OK: dados preservados após reinício do backend"
else
  echo "FALHA: dados perdidos após reinício do backend" >&2
  exit 1
fi