<!--
SYNC IMPACT REPORT (temporary — remove before committing)
Version change: n/a → 1.0.0
Modified principles: none (initial creation)
Added sections: Core Principles (5), Restrições Técnicas, Fluxo de Desenvolvimento e Qualidade, Governance
Removed sections: none
TODOs: nenhum placeholder adiado
-->
# Constituição do Todo-App

## Core Principles

### I. Simplicidade

Simplicidade é a prioridade máxima e é não negociável. Todo código, API e
configuração DEVE começar pela solução mais simples que atenda ao requisito
atual (YAGNI). É PROIBIDO adicionar abstrações, dependências ou configurações
antecipando necessidades hipotéticas. Código óbvio é preferível a código
inteligente; quando uma solução complexa for necessária, ela DEVE ser
justificada e documentada.

*Rationale:* Complexidade é a principal fonte de bugs, retrabalho e custo de
manutenção. Simplicidade acelera mudanças e reduz o custo de governança.

### II. Código Limpo (Clean Code)

Todo código DEVE seguir os princípios de Clean Code: nomes significativos,
funções pequenas com responsabilidade única, ausência de duplicação (DRY) e
sem efeitos colaterais ocultos. Cada componente DEVE ter um propósito claro e
legível sem comentários desnecessários. Nenhuma regra de Clean Code pode ser
ignorada sem revisão justificada.

*Rationale:* Código limpo é o pré-requisito para revisão eficaz, testes
confiáveis e evolução segura do sistema.

### III. Testes Automatizados (NON-NEGOTIABLE)

Testes automatizados são obrigatórios para todo comportamento entregue. Todo
novo recurso ou correção DEVE ser acompanhado de testes que validem o
comportamento. Os testes DEVEM ser determinísticos, rápidos e executados de
forma automatizada na integração contínua. Nenhum código entra na branch
principal sem passar na suíte de testes.

*Rationale:* Testes são a evidência objetiva de que o comportamento esperado
funciona e a rede de segurança que permite refatoração contínua.

### IV. Arquitetura Desacoplada

O sistema DEVE ser dividido em camadas independentes com contratos explícitos.
O front-end DEVE ser escrito em JavaScript Vanilla e DEVE consumir a API
RESTful por meio de HTTP/JSON, sem conhecimento do banco de dados nem da
implementação do servidor. A API RESTful DEVE expor recursos de forma
estável e versionada. É PROIBIDO acoplar front-end e back-end por outras vias
que não o contrato RESTful.

*Rationale:* Desacoplamento permite evoluir cada camada de forma independente,
testar isoladamente e substituir componentes sem reescrita do todo.

### V. Banco de Dados Leve

O banco de dados DEVE ser leve, embutido ou de fácil configuração, sem exigir
infraestrutura externa ou serviços dedicados para desenvolvimento. A
configuração DEVE ser simples e reproduzível localmente com um único comando
ou um único arquivo. O uso de banco de dados pesado ou externo apenas se
aplicável à produção DEVE ser justificado por escrito.

*Rationale:* Dependências de infraestrutura pesada aumentam o custo de setup,
dificultam testes e violam o princípio da simplicidade.

## Restrições Técnicas

A stack DEVE permanecer compacta e alinhada aos princípios:

- Front-end: JavaScript Vanilla (HTML/CSS/JS puros), sem frameworks.
- Comunicação: consumo da API RESTful via HTTP/JSON; contratos com
  semântica e respostas de erro padronizadas.
- Back-end: Em Java com Springboot expõe exclusivamente a API RESTful; não emite HTML de UI.
- Dados:Algum banco banco leve e de fácil configuração (ex.: SQLite ou equivalente);
  migrações versionadas e reprodutíveis.
- New dependencies (Novas dependências): só após justificativa escrita que
  comprove necessidade; dependências que quebrem a simplicidade ou exijam
  infraestrutura externa DEVEM ser rejeitadas na revisão.

## Fluxo de Desenvolvimento e Qualidade

O fluxo DEVE aplicar os seguintes portões de qualidade:

- Todo recurso começa pela definição de teste antes da implementação; a
  suíte DEVE falhar ao cobrir um requisito ainda não implementado e passar
  após a implementação (ciclo red-green-refactor).
- Nenhum merge sem testes de unidade relevantes e suíte completa verde.
- Revisão de código DEVE verificar compliance com os cinco princípios;
  aprovação é condição obrigatória para merge.
- Mudança de contrato da API DEVE acompanhar migração e atualização dos
  testes de integração.
- Código com duplicação, abstrações não justificadas ou complexidade
  desnecessária DEVE ser rejeitado na revisão.

## Governance

Esta constituição substitui todas as demais práticas em conflito. Qualquer
discrepância entre documentação e esta constituição é resolvida em favor da
constituição.

- Procedimento de emenda: toda emenda DEVE documentar a mudança, justificativa
  e plano de migração, e ser aprovada antes de aplicada.
- Política de versionamento: a versão segue semver. MAJOR para remoção ou
  redefinição de princípio; MINOR para novo princípio ou expansão material de
  orientação; PATCH para esclarecimentos e ajustes de redação.
- Geração de mudanças: cada alteração DEVE atualizar a versão e o campo "Last
  Amended" com data ISO no formato AAAA-MM-DD.
- Compliance: toda revisão de código e toda tarefa DEVE verificar se a decisão
  respeita simplicidade, código limpo, testes automatizados, desacoplamento e
  banco de dados leve; descumprimentos DEVEM ser apontados e corrigidos.
- Complexidade e dependências não previstas DEVEM ser justificadas por escrito
  antes de serem aceitas.

**Version**: 1.0.0 | **Ratified**: 2026-09-11 | **Last Amended**: 2026-09-11
