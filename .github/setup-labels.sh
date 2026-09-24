#!/bin/bash

# Configuração
REPO="rezluis/todo-app"

# Função auxiliar
create_label() {
  local name="$1"
  local color="$2"
  local description="$3"
  
  gh label create "$name" \
    --color "$color" \
    --description "$description" \
    --repo "$REPO" \
    --force 2>/dev/null && echo "✅ $name" || echo "❌ $name"
}

echo "🏷️  Criando labels para $REPO..."

# Tipo de Manutenção (ISO 14764)
create_label "type/corrective" "d73a4a" "Correção de defeitos e falhas (ISO 14764)"
create_label "type/adaptive" "0075ca" "Adaptação a mudanças no ambiente (ISO 14764)"
create_label "type/perfective" "a2eeef" "Melhoria de desempenho ou manutenibilidade (ISO 14764)"
create_label "type/preventive" "fbca04" "Prevenção de problemas futuros (ISO 14764)"

# Prioridade
create_label "priority/P0" "b60205" "Crítico - Sistema fora do ar, perda de dados, segurança"
create_label "priority/P1" "d93f0b" "Alto - Funcionalidade principal quebrada"
create_label "priority/P2" "fbca04" "Médio - Bug menor ou melhoria útil"
create_label "priority/P3" "c2e0c6" "Baixo - Melhorias nice-to-have"

# Severidade
create_label "severity/critical" "b60205" "Sistema indisponível ou dados comprometidos"
create_label "severity/high" "d93f0b" "Funcionalidade principal afetada"
create_label "severity/medium" "fbca04" "Funcionalidade secundária afetada"
create_label "severity/low" "c2e0c6" "Cosmético ou impacto mínimo"

# Triagem e Workflow
create_label "triage" "ededed" "Aguardando triagem inicial"
create_label "status/analyzed" "5319e7" "Análise de impacto concluída"
create_label "status/in-progress" "1d76db" "Em implementação"
create_label "status/blocked" "e99695" "Bloqueada por outra issue"
create_label "status/needs-info" "d4c5f9" "Aguardando informações do autor"
create_label "status/wontfix" "ffffff" "Não será corrigida"
create_label "status/duplicate" "cfd3d7" "Duplicata de outra issue"

# Área / Módulo
create_label "area/backend" "006b75" "Afeta o backend (API, serviços)"
create_label "area/frontend" "0e8a16" "Afeta o frontend (UI/UX)"
create_label "area/database" "5319e7" "Afeta banco de dados ou migrations"
create_label "area/api" "1d76db" "Afeta contratos de API pública"
create_label "area/auth" "b60205" "Afeta autenticação/autorização"
create_label "area/infra" "6f42c1" "Afeta infraestrutura, CI/CD, deploy"
create_label "area/docs" "0075ca" "Afeta documentação"
create_label "area/tests" "a2eeef" "Afeta testes automatizados"

# Tipo de Trabalho
create_label "enhancement" "a2eeef" "Nova funcionalidade ou melhoria"
create_label "refactor" "d4c5f9" "Refatoração sem mudança de comportamento"
create_label "chore" "fef2c0" "Tarefas de manutenção geral"
create_label "documentation" "0075ca" "Melhorias na documentação"
create_label "question" "d876e3" "Dúvida ou discussão"

# Labels Especiais
create_label "security" "b60205" "Vulnerabilidade ou risco de segurança"
create_label "dependencies" "0366d6" "Atualização de dependências"
create_label "breaking-change" "b60205" "Mudança que quebra compatibilidade"
create_label "good-first-issue" "7057ff" "Ideal para novos contribuidores"
create_label "help-wanted" "008672" "Precisa de ajuda da comunidade"
create_label "blocked-by" "e99695" "Bloqueada por outra issue"
create_label "blocking" "d93f0b" "Bloqueia outras issues"

echo "✅ Labels criadas com sucesso!"
