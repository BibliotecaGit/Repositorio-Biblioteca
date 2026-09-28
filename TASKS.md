# Backlog Detalhado — Aurora: Sistema de Biblioteca com Aluguel de Livros

**Especificação de origem:** Aurora v3.0 (2026-09-28)
**Formato:** Este documento quebra cada Requisito Funcional (RF) em tarefas executáveis, com subtarefas, critérios de aceite mapeados aos CAs da spec, dependências e notas técnicas.
**Observação v3.0:** A funcionalidade de Fila de Espera / Lista de Pendentes **não existe**. Livro indisponível = todas as 3 cópias alugadas → botão "Alugar" desabilitado até devolução de uma cópia (RN-003).

---

## Convenções

- **Prioridade:** 🔴 Alta / 🟡 Média / 🟢 Baixa
- **Tamanho estimado:** P (≤ 1 dia), M (2–3 dias), G (4+ dias)
- Referências: `RF-XXX` (requisito), `CA-XXX.X` (critério de aceite), `RN-00X` (regra de negócio), `INT-X` (integridade/transação), `RNF-0XX` (não-funcional)

---

# FASE 0 — FUNDAÇÃO

## F0.1 — Setup do projeto e CI/CD
**Prioridade:** 🔴 | **Tamanho:** P

**Descrição:** Criar a estrutura base do projeto (backend, frontend, banco), ferramentas de qualidade e pipeline de CI.

**Subtarefas:**
- [ ] Inicializar repositório com estrutura de pastas (backend / frontend / infra)
- [ ] Configurar lint (ESLint/Prettier ou equivalente) e convenções de código
- [ ] Configurar suíte de testes (unitários, integração, E2E)
- [ ] Configurar CI: build + lint + testes em cada push/PR
- [ ] Documentar setup local no README (como subir o ambiente com 1 comando)

**Critérios de aceite:**
- [ ] Pipeline verde em `main`
- [ ] Build reproduzível a partir do zero

---

## F0.2 — Migrations do banco de dados (MySQL 8.0)
**Prioridade:** 🔴 | **Tamanho:** M
**Depende de:** F0.1

**Descrição:** Implementar todas as tabelas conforme o design físico (§6.3 da spec), enums consolidados (§6.2), índices (§6.4) e regras de integridade (§6.5).

**Subtarefas:**
- [ ] Configurar conexão MySQL 8.0+, engine **InnoDB**, charset **utf8mb4 / utf8mb4_0900_ai_ci**
- [ ] Migration: `USUARIO` (com `senha_hash`, `tipo`, `idioma`, `tema`, endereço, cartão tokenizado/mascarado, `bloqueado_ate`, `deleted_at`, `created_at`)
- [ ] Migration: `LIVRO` (com `faixa_tamanho` ENUM, `valor_livro`, `preco_aluguel`, `capa_url`, `data_lancamento`, `avaliacao_media`, `total_avaliacoes`, `alugueis_ultimo_ano`, `eh_infantil`)
- [ ] Migration: `EXEMPLAR` (com `codigo_barras` UNIQUE, `status` ENUM, FK para LIVRO com `ON DELETE RESTRICT`)
- [ ] Migration: `ALUGUEL` (com `data_devolucao_prevista`, `data_devolucao_real`, `extensao_contador` CHECK ≤ 2, `preco_cobrado`, FKs RESTRICT)
- [ ] Migration: `JA_LIDOS` (UNIQUE `id_usuario + id_livro`, CASCADE)
- [ ] Migration: `LISTA_DESEJOS` (UNIQUE `id_usuario + id_livro`, CASCADE)
- [ ] Migration: `AVALIACAO` (nota CHECK 0–5, UNIQUE `id_usuario + id_livro`, CASCADE)
- [ ] Migration: `USO_FILTRO` (ENUM tipo_filtro, UNIQUE `id_usuario + tipo_filtro + valor`, CASCADE)
- [ ] Migration: `MULTA` (ENUMs tipo/status, `dias_atraso`, `data_pagamento`, FKs RESTRICT)
- [ ] Criar todos os índices do §6.4 com justificativas:
  - [ ] `LIVRO(titulo)`, `LIVRO(autor)`, `LIVRO(genero)` (e avaliar FULLTEXT em `titulo, autor`)
  - [ ] `LIVRO(faixa_tamanho)`, `LIVRO(data_lancamento)`, `LIVRO(eh_infantil)`, `LIVRO(avaliacao_media)`, `LIVRO(alugueis_ultimo_ano)`
  - [ ] `EXEMPLAR(id_livro, status)`
  - [ ] `ALUGUEL(data_devolucao_prevista)` (job de atrasos)
  - [ ] `ALUGUEL(id_usuario, status)` (elegibilidade)
  - [ ] `MULTA(id_usuario, status)`
  - [ ] `USUARIO(email)` UNIQUE

**Critérios de aceite:**
- [ ] Migrations versionadas e **reversíveis** (uma por tabela/alteração)
- [ ] `deleted_at` presente nas tabelas com soft delete (LIVRO, USUARIO)
- [ ] `created_at DATETIME DEFAULT CURRENT_TIMESTAMP` em todas as tabelas

---

## F0.3 — Seeds obrigatórios
**Prioridade:** 🔴 | **Tamanho:** P
**Depende de:** F0.2

**Descrição:** Popular o banco com dados mínimos de operação (§6.7).

**Subtarefas:**
- [ ] Seed: 1 administrador, 1 bibliotecário, 1 aluno de teste (senhas documentadas apenas para dev)
- [ ] Seed: catálogo de exemplo cobrindo **todas as 4 faixas de tamanho** (RN-001)
- [ ] Seed: gêneros principais (romance, drama, ficção científica, fantasia, suspense, terror, autoajuda, biografia, história, poesia, infantil)
- [ ] Seed: títulos com `data_lancamento` recente (≤ 6 meses) → alimenta Novidades
- [ ] Seed: títulos com boa avaliação e histórico → alimenta Destaques
- [ ] Seed: cada título com 3 exemplares (status "disponível")

**Critérios de aceite:**
- [ ] Seeds executam de forma idempotente
- [ ] Ao subir o ambiente, as abas Início/Prateleira/Sugestões já têm conteúdo

---

## F0.4 — Autenticação base (hash + sessão)
**Prioridade:** 🔴 | **Tamanho:** M
**Depende de:** F0.2

**Descrição:** Mecanismos de segurança de autenticação usados por todos os módulos.

**Subtarefas:**
- [ ] Hash de senha com **bcrypt/argon2** (RNF-005)
- [ ] Emissão/controle de sessão com **expiração de 30 min de inatividade** (RNF-006)
- [ ] Middleware/guard de autenticação para rotas protegidas
- [ ] Estrutura de perfil de usuário na sessão (id, tipo, tema, idioma)

**Critérios de aceite:**
- [ ] Senha nunca armazenada/em logada em claro
- [ ] Sessão expira após 30 min sem atividade

---

# FASE 1 — AUTENTICAÇÃO E ACESSO

## F1.1 — Cadastro de usuário (RF-001)
**Prioridade:** 🔴 | **Tamanho:** M
**Depende de:** F0.4

**Descrição:** Permitir cadastro com nome, e-mail e senha, com auto-login e redirect para a área logada.

**Subtarefas:**
- [ ] Tela de cadastro com campos nome, e-mail, senha
- [ ] Validação de campos obrigatórios (CA-001.1)
- [ ] Validação de formato de e-mail + unicidade (CA-001.2; fluxo 3a: "Este e-mail já está em uso")
- [ ] Validação de senha ≥ 6 caracteres (CA-001.3; fluxo 3b)
- [ ] Criar conta com `tipo = 'aluno'` (CA-001.4)
- [ ] Autenticar automaticamente após cadastro e redirecionar para aba **Início** (CA-001.5)

**Critérios de aceite:** CA-001.1 – CA-001.5

---

## F1.2 — Login (RF-002)
**Prioridade:** 🔴 | **Tamanho:** M
**Depende de:** F0.4

**Subtarefas:**
- [ ] Tela de login com e-mail e senha
- [ ] Login obrigatório para área logada, aluguel, avaliação e coleções (CA-002.1)
- [ ] Mensagem **genérica** "E-mail ou senha incorretos" (CA-002.2 — não revelar qual campo falhou)
- [ ] Redirect para Início após login (CA-002.3)
- [ ] Sessão expira após 30 min de inatividade (CA-002.4)

**Critérios de aceite:** CA-002.1 – CA-002.4

---

## F1.3 — Controle de acesso por perfil (RF-003)
**Prioridade:** 🔴 | **Tamanho:** M
**Depende de:** F1.2

**Subtarefas:**
- [ ] Guard de rotas por tipo (`aluno`, `bibliotecario`, `administrador`)
- [ ] Aluno: acesso à sidebar completa + aluguel + histórico (CA-003.1)
- [ ] Bibliotecário: telas de gerenciamento de acervo + aplicação de multa por dano (CA-003.2)
- [ ] Administrador: categorização de títulos + exclusão de títulos (CA-003.3)
- [ ] Bloqueio de acesso indevido com redirect/mensagem apropriada

**Critérios de aceite:** CA-003.1 – CA-003.3

---

## F1.4 — Logout e proteção de rotas (RF-030)
**Prioridade:** 🔴 | **Tamanho:** P
**Depende de:** F1.2

**Subtarefas:**
- [ ] Item "Sair" na sidebar encerra a sessão e redireciona para login/cadastro (CA-030.1)
- [ ] Acesso à área logada sem sessão → redirect para login (CA-030.2)
- [ ] Garantir que tema, idioma e região permanecem salvos no perfil (CA-030.3 — revalidado de forma abrangente em F7)

**Critérios de aceite:** CA-030.1 – CA-030.3

---

# FASE 2 — ACERVO

## F2.1 — Cadastro de livro (RF-004)
**Prioridade:** 🔴 | **Tamanho:** M
**Depende de:** F1.3 (perfil bibliotecário), F0.2

**Descrição:** Cadastro de títulos com cálculo automático de tamanho e criação das 3 cópias.

**Subtarefas:**
- [ ] Formulário: título, autor, gênero, número de páginas, valor do livro, **preço do aluguel**, capa (upload/URL) e data de lançamento
- [ ] Todos os campos obrigatórios, exceto data de lançamento (CA-004.1)
- [ ] Implementar cálculo automático das **4 faixas** (CA-004.2 / RN-001):
  - [ ] Pequeno: até 100 páginas
  - [ ] Médio-pequeno: > 100 até 150
  - [ ] Médio-padrão: > 150 até 250
  - [ ] Grande: > 250
- [ ] Se gênero = "Infantil", marcar `eh_infantil = 1` (alimenta filtro Infantis, CA-008.3)
- [ ] Registrar `valor_livro` (teto de multa) e `preco_aluguel` (exibido no hover) (CA-004.4)
- [ ] Ao criar o título, criar **3 exemplares** com status "disponível" (CA-004.5) — na mesma transação
- [ ] Livro sem data de lançamento **não aparece** em Novidades (CA-004.1)

**Critérios de aceite:** CA-004.1 – CA-004.5

---

## F2.2 — Cadastro de exemplar (RF-005)
**Prioridade:** 🔴 | **Tamanho:** M
**Depende de:** F2.1

**Subtarefas:**
- [ ] Tela/fluxo de adição de exemplar vinculado a livro existente (CA-005.1)
- [ ] Campo código de barras opcional; quando informado, **único** (CA-005.2)
- [ ] Status inicial "disponível" (CA-005.3)
- [ ] **Enforce de máximo 3 exemplares por título dentro de transação** (CA-005.4 / INT-2)
- [ ] Permitir 1 a 3 exemplares (padrão 3)

**Critérios de aceite:** CA-005.1 – CA-005.4 | **Nota técnica:** verificação de contagem deve usar `SELECT ... FOR UPDATE` no LIVRO para evitar corrida (INT-1).

---

## F2.3 — Edição de livro (RF-006)
**Prioridade:** 🟡 | **Tamanho:** M
**Depende de:** F2.1

**Subtarefas:**
- [ ] Tela de edição de livro
- [ ] Alteração de páginas → recalcular `faixa_tamanho` (CA-006.1)
- [ ] Alteração de `valor_livro` → vale para multas **futuras**; multas já geradas não mudam (CA-006.2)
- [ ] Alteração de `preco_aluguel` → reflete imediatamente no hover da capa (CA-006.3)

**Critérios de aceite:** CA-006.1 – CA-006.3

---

## F2.4 — Exclusão lógica de título (RF-007)
**Prioridade:** 🟡 | **Tamanho:** P
**Depende de:** F2.1, F1.3 (perfil admin)

**Subtarefas:**
- [ ] Botão de exclusão visível apenas para administrador
- [ ] Bloquear exclusão se houver **qualquer exemplar com status "alugado"** (CA-007.1)
- [ ] Implementar soft delete (`deleted_at`) com confirmação explícita (CA-007.2 / INT-6)
- [ ] Garantir que **todas** as consultas do catálogo filtram `deleted_at IS NULL` (auditoria geral)

**Critérios de aceite:** CA-007.1 – CA-007.2

---

## F2.5 — Categorização de títulos (RF-008)
**Prioridade:** 🟡 | **Tamanho:** M
**Depende de:** F2.1, F1.3 (perfil admin)

**Subtarefas:**
- [ ] Admin pode editar gênero de qualquer livro (CA-008.1)
- [ ] Admin pode **sobrescrever manualmente** a faixa de tamanho (CA-008.2) — definir como persistir a sobreposição (ex.: flag `tamanho_manual`)
- [ ] Gênero "Infantil" habilita o livro na aba Infantis da Prateleira (CA-008.3)
- [ ] Mudanças de categoria não afetam aluguéis em andamento (CA-008.4)

**Critérios de aceite:** CA-008.1 – CA-008.4

---

# FASE 3 — NÚCLEO DE ALUGUEL

## F3.1 — Vitrine de livros com hover e clique (RF-009)
**Prioridade:** 🔴 | **Tamanho:** G
**Depende de:** F2.1, F5.1 (layout logado — pode ser desenvolvido em paralelo com stub)

**Descrição:** Componente de vitrine reutilizado em Início, Prateleira, Sugestões, Já lidos, Lista de desejos e Avaliar.

**Subtarefas:**
- [ ] Componente de vitrine: capas lado a lado, layout responsivo (CA-009.1)
- [ ] **Hover** na capa → overlay com: nome do livro, preço do aluguel, avaliação média em estrelas (CA-009.2)
- [ ] **Clique** na capa → painel de ação com botão **"Alugar"** (CA-009.3)
- [ ] Estado do botão "Alugar":
  - [ ] Habilitado quando ≥ 1 das 3 cópias está "disponível"
  - [ ] Desabilitado quando todas alugadas/indisponíveis (CA-009.3)
- [ ] Busca parcial por título, autor ou gênero na vitrine (CA-009.4)
- [ ] Ações do clique também incluem: "Marcar como Lido", "Adicionar à Lista de Desejos" (integração com F6)

**Critérios de aceite:** CA-009.1 – CA-009.4 | RNF-015

---

## F3.2 — Aluguel de livro disponível (RF-010) ⚠️ ALTO RISCO
**Prioridade:** 🔴 | **Tamanho:** G
**Depende de:** F3.1, F0.2

**Descrição:** Operação central do sistema. Deve ser **atômica** para evitar corrida nas 3 cópias.

**Subtarefas:**
- [ ] Endpoint/serviço de aluguel envolvido em **transação** (INT-1)
- [ ] `SELECT ... FOR UPDATE` em EXEMPLAR/LIVRO para seleção da cópia (INT-1)
- [ ] Validar elegibilidade do usuário (todas com mensagens específicas, CA-010.2–10.4 / RN-006):
  - [ ] Sem multas pendentes (consulta `MULTA` status = pendente)
  - [ ] Não bloqueado (`bloqueado_ate` nulo ou < hoje; atraso ativo também impede)
  - [ ] Menos de 3 aluguéis com status "ativo"
- [ ] Exigir autenticação (CA-010.1)
- [ ] Se não houver cópia disponível → "Alugar" desabilitado; livro só volta a ficar disponível após devolução (CA-010.5 / RN-003)
- [ ] Selecionar automaticamente um exemplar disponível e cobrar o `preco_aluguel` (CA-010.6 / RN-008)
- [ ] Registrar cobrança no cartão do usuário (integração F7.1)
- [ ] Calcular `data_devolucao_prevista`: hoje + 15 dias (≤ 150 páginas) ou + 30 dias (> 150) (CA-010.7 / RN-001)
- [ ] Persistir `preco_cobrado` (preço no momento do aluguel)
- [ ] Alterar status do exemplar → "alugado" (CA-010.8)
- [ ] Incrementar `LIVRO.alugueis_ultimo_ano` (alimenta Destaques)
- [ ] Enviar notificação de confirmação (e-mail + in-app) (CA-010.9)
- [ ] Tela de confirmação com prazo de devolução (passo 10 do UC-003)

**Critérios de aceite:** CA-010.1 – CA-010.9 | INT-1, INT-3, RN-003, RN-006, RN-008
**Nota técnica:** escrever testes de concorrência (dois usuários alugando a última cópia simultaneamente → apenas um sucesso).

---

## F3.3 — Devolução de livro (RF-011)
**Prioridade:** 🔴 | **Tamanho:** G
**Depende de:** F3.2

**Subtarefas:**
- [ ] Fluxo de devolução (usuário inicia pelo sistema e/ou bibliotecário pela tela de staff)
- [ ] Validar que o exemplar está emprestado para aquele usuário; senão, erro "Este livro não está emprestado" (CA-011.1)
- [ ] Verificar atraso: `data_real > data_prevista`
- [ ] Se houver atraso, gerar MULTA tipo "atraso": `dias_atraso × R$ 3,00`, **teto = valor_livro** (CA-011.2 / RN-004)
- [ ] Atualizar `data_devolucao_real`, status do aluguel → "devolvido"
- [ ] Alterar status do exemplar → **"disponível" imediatamente**, elegível a novo aluguel (CA-011.3)
- [ ] Notificação de confirmação de devolução (e-mail + in-app) (CA-011.4)
- [ ] Processamento independente de múltiplas devoluções (CA-011.5)

**Critérios de aceite:** CA-011.1 – CA-011.5 | INT-3

---

## F3.4 — Solicitação de extensão de prazo (RF-012)
**Prioridade:** 🔴 | **Tamanho:** M
**Depende de:** F3.2

**Subtarefas:**
- [ ] Listar aluguéis ativos do usuário com opção "Solicitar extensão"
- [ ] Exibir opção apenas quando `extensao_contador < 2` (CA-012.1)
- [ ] 1ª extensão: nova `data_devolucao_prevista` = hoje + 2 meses; contador → 1 (CA-012.2)
- [ ] 2ª extensão: hoje + 1 mês; contador → 2 (CA-012.3)
- [ ] Ação **explícita** do usuário (nunca automática) (CA-012.4 / RN-002)
- [ ] Após 2ª extensão: ocultar opção e notificar que a devolução é obrigatória (CA-012.5)

**Critérios de aceite:** CA-012.1 – CA-012.5 | RN-002

---

## F3.5 — Indicadores denormalizados e recálculos
**Prioridade:** 🟡 | **Tamanho:** M
**Depende de:** F3.2

**Subtarefas:**
- [ ] Incrementar `LIVRO.alugueis_ultimo_ano` a cada aluguel
- [ ] **Job mensal** para zerar/remover registros com mais de 1 ano (manter coerência com RN-011)
- [ ] Recálculo de `avaliacao_media`/`total_avaliacoes` a cada insert/update em AVALIACAO (aplicação ou trigger — decidir e documentar)
- [ ] Garantir RNF-004: recálculo ≤ 1 segundo

**Critérios de aceite:** §6.6 | RNF-004

---

# FASE 4 — MULTAS, PENALIDADES E NOTIFICAÇÕES

## F4.1 — Job diário de atrasos (RF-013)
**Prioridade:** 🔴 | **Tamanho:** G
**Depende de:** F3.2, F0.2

**Subtarefas:**
- [ ] Agendar job diário às **00:00** (RNF-003)
- [ ] Buscar aluguéis ativos com `data_devolucao_prevista < hoje` (usa índice `ALUGUEL(data_devolucao_prevista)`)
- [ ] Calcular `dias_atraso = hoje − data_prevista` (CA-013.2)
- [ ] `valor_multa = dias_atraso × R$ 3,00` (CA-013.3)
- [ ] Aplicar teto: se `valor_multa ≥ valor_livro` → `valor_multa = valor_livro` (CA-013.4)
- [ ] Se `dias_atraso > 15` → bloquear usuário por 1 semana: `bloqueado_ate = hoje + 7 dias` (CA-013.5)
- [ ] Se teto atingido e atraso continua: +1 dia de bloqueio por dia adicional (CA-013.6)
- [ ] Criar/atualizar registro de MULTA tipo "atraso" (evitar duplicidade em execuções diárias — decidir: upsert ou multa acumulada por execução)
- [ ] Usuário com multa pendente ou bloqueio ativo **não consegue alugar** (CA-013.7 / RN-006) — já aplicado em F3.2, validar integração

**Critérios de aceite:** CA-013.1 – CA-013.7 | RN-004, RN-006, RNF-003

---

## F4.2 — Multa por dano ao exemplar (RF-014)
**Prioridade:** 🟡 | **Tamanho:** M
**Depende de:** F3.3, F1.3 (perfil bibliotecário)

**Subtarefas:**
- [ ] Na tela de devolução (staff), opção "Aplicar multa por dano" (CA-014.1)
- [ ] Dropdown de tipos de dano (capa rasgada, páginas faltantes, etc.) (CA-014.2)
- [ ] Valor fixo **R$ 20,00 readonly** (CA-014.3)
- [ ] Cumulativa com multa de atraso (CA-014.4)
- [ ] Registrar histórico de danos por exemplar (CA-014.5)
- [ ] Status do exemplar → "danificado" (UC-008)
- [ ] Notificar o usuário (e-mail + in-app)

**Critérios de aceite:** CA-014.1 – CA-014.5 | RN-005

---

## F4.3 — Visualização de multas pelo usuário (RF-015)
**Prioridade:** 🟡 | **Tamanho:** M
**Depende de:** F4.1, F4.2

**Subtarefas:**
- [ ] Tela de multas com: tipo, valor, dias de atraso (quando houver), status (pendente/pago), data de geração (CA-015.1)
- [ ] Marcagem de multas pagas como "pago" (CA-015.2) — definir fluxo de pagamento (está fora do escopo detalhado da spec; registrar a cobrança)

**Critérios de aceite:** CA-015.1 – CA-015.2

---

## F4.4 — Serviço de notificações (RF-016 / RN-007)
**Prioridade:** 🔴 | **Tamanho:** G
**Depende de:** F3.2 (primeiro consumidor)

**Subtarefas:**
- [ ] Serviço central de notificações com **canal duplo**: e-mail + in-app, sempre juntos (CA-016.5)
- [ ] Notificação de **confirmação de aluguel** (CA-016.1)
- [ ] Notificação de **prazo próximo — 3 dias antes do vencimento** (CA-016.2 / RN-007) — job diário próprio ou no mesmo job de F4.1
- [ ] Notificação de **atraso detectado** (CA-016.3)
- [ ] Notificação de **devolução obrigatória após máximo de extensões** (CA-016.4)
- [ ] Notificação de confirmação de devolução
- [ ] Central de notificações in-app (listagem com lidas/não lidas)

**Critérios de aceite:** CA-016.1 – CA-016.5 | RN-007
**Nota técnica:** em dev, usar mail catcher/logger; definir provedor de e-mail para produção.

---

# FASE 5 — NAVEGAÇÃO E DESCOBERTA

## F5.1 — Sidebar e layout da área logada (RF-017)
**Prioridade:** 🔴 | **Tamanho:** M
**Depende de:** F1.2

**Subtarefas:**
- [ ] Layout com sidebar persistente em todas as telas logadas (CA-017.1)
- [ ] Itens: Início, Prateleira, Sugestões, Já lidos, Lista de desejos, Avaliar livros, Configurações, Sair (CA-017.2)
- [ ] Item ativo destacado em `#8CC3F5` (CA-017.3)
- [ ] Rotas para cada seção (stub inicial das telas destino)
- [ ] Sidebar adaptável (mobile: colapsar) — RNF-009
- [ ] Indicador de notificações (integração com F4.4)

**Critérios de aceite:** CA-017.1 – CA-017.3 | RNF-009

---

## F5.2 — Início (RF-018)
**Prioridade:** 🔴 | **Tamanho:** M
**Depende de:** F5.1, F3.1

**Subtarefas:**
- [ ] Seção de descrição institucional no topo (CA-018.1):
  - [ ] Conteúdo padrão: apresentação da Aurora como biblioteca comunitária de aluguel, missão de incentivo à leitura, acervo com 3 cópias por título
  - [ ] Conteúdo **editável pelo administrador** (campo de texto institucional — decidir onde persistir: tabela de configurações ou campo dedicado)
- [ ] Seção "Novidades": livros com `data_lancamento` nos últimos 6 meses, com vitrine (hover/clique conforme RF-009) (CA-018.2)
- [ ] Fallback: sem lançamentos no período → mensagem amigável + livros mais recentes cadastrados (CA-018.3)

**Critérios de aceite:** CA-018.1 – CA-018.3 | RN-011

---

## F5.3 — Prateleira com 6 filtros combináveis (RF-019)
**Prioridade:** 🔴 | **Tamanho:** G
**Depende de:** F5.1, F3.1, F0.2 (índices)

**Subtarefas:**
- [ ] Título da seção: **"Escolher filtros:"** (RF-019)
- [ ] Filtro **Por Tamanho** — 4 faixas (CA-019.1 / RN-001)
- [ ] Filtro **Por Gênero** — gêneros do acervo + lista de principais (CA-019.2)
- [ ] Filtro **Por Autor** — autores do acervo ordenados por relevância (CA-019.3)
- [ ] Filtro **Novidades** — últimos 6 meses (CA-019.4 / RN-011)
- [ ] Filtro **Destaques** — aluguéis no último ano acima do percentil 80 **E** avaliação média ≥ 4,5 (CA-019.5 / RN-011)
- [ ] Filtro **Infantis** — gênero "Infantil" (CA-019.6 / CA-008.3)
- [ ] Filtros **combináveis** (CA-019.7)
- [ ] Resultados na vitrine com hover e clique (CA-019.8)
- [ ] Performance: resposta ≤ 2s em até 10.000 livros (RNF-001 — usar índices do §6.4; avaliar FULLTEXT)

**Critérios de aceite:** CA-019.1 – CA-019.8 | RN-011, RNF-001

---

## F5.4 — Registro de uso de filtros (motor de Sugestões)
**Prioridade:** 🟡 | **Tamanho:** M
**Depende de:** F5.3

**Subtarefas:**
- [ ] A cada aplicação de filtro (tamanho, gênero, autor), incrementar contagem em `USO_FILTRO` (CA-020.1)
- [ ] Algoritmo de sugestão, prioridade: (1) gênero mais usado, (2) autor mais usado, (3) faixa de tamanho mais usada (CA-020.2 / RN-010)
- [ ] Rebaixar prioridade de livros com aluguel ativo do usuário ou marcados como "Lido" (CA-020.3)
- [ ] Exibir o motivo da sugestão: "Porque você lê muito Romance" (CA-020.4)

**Critérios de aceite:** CA-020.1 – CA-020.4 | RN-010

---

## F5.5 — Aba Sugestões (RF-020)
**Prioridade:** 🟡 | **Tamanho:** P
**Depende de:** F5.4, F3.1

**Subtarefas:**
- [ ] Renderizar sugestões do motor F5.4 na aba Sugestões
- [ ] Exibir motivo em cada card (CA-020.4)
- [ ] Sugestões respeitam a vitrine padrão (hover, clique, botão Alugar)

**Critérios de aceite:** CA-020.1 – CA-020.4

---

# FASE 6 — COLEÇÕES E AVALIAÇÕES

## F6.1 — Já Lidos (RF-021)
**Prioridade:** 🔴 | **Tamanho:** M
**Depende de:** F5.1, F3.1

**Subtarefas:**
- [ ] Opção "Marcar como Lido" no catálogo e no painel de ações do livro (CA-021.1)
- [ ] Inserir em `JA_LIDOS` (UNIQUE por usuário+livro)
- [ ] Aba Já lidos: listagem com capa, nome, autor e avaliação do usuário (se houver) (CA-021.2)
- [ ] Remover marcação de "Lido" (CA-021.3)
- [ ] Ao desmarcar "Lido", avaliação existente deve ser tratada (remover ou bloquear desmarcação — decidir e documentar; RN-009 amarra avaliação a "Lido")
- [ ] Apenas "Lido" pode ser avaliado (CA-021.4 — enforcement em F6.3)

**Critérios de aceite:** CA-021.1 – CA-021.4 | INT-4, RN-009

---

## F6.2 — Lista de Desejos (RF-022)
**Prioridade:** 🔴 | **Tamanho:** M
**Depende de:** F5.1, F3.1

**Subtarefas:**
- [ ] Opção "Adicionar à Lista de Desejos" no catálogo e no painel de ações (CA-022.1)
- [ ] Inserir em `LISTA_DESEJOS` (UNIQUE por usuário+livro)
- [ ] Aba Lista de desejos: capa, nome, autor, preço do aluguel e avaliação (CA-022.2)
- [ ] Remover livro da lista (CA-022.3)

**Critérios de aceite:** CA-022.1 – CA-022.3

---

## F6.3 — Avaliar livros (RF-023)
**Prioridade:** 🔴 | **Tamanho:** G
**Depende de:** F6.1, F0.2

**Subtarefas:**
- [ ] Aba "Avaliar livros" com três seções (CA-023.4):
  - [ ] (a) Livros lidos aguardando avaliação
  - [ ] (b) Avaliações já realizadas (editáveis)
  - [ ] (c) Avaliações médias de livros não lidos — somente leitura
- [ ] Permitir avaliação **somente** se `EXISTS` em `JA_LIDOS` (CA-023.1 / INT-4 / RN-009)
- [ ] Nota de 0 a 5 estrelas (decimais, ex.: 4,5)
- [ ] Alterar avaliação a qualquer momento (CA-023.2)
- [ ] Recalcular `avaliacao_media`/`total_avaliacoes` automaticamente, ≤ 1s (CA-023.3 / RNF-004)
- [ ] Anonimato: outros usuários veem apenas a média (CA-023.5)
- [ ] Tentativa de avaliar não lido → mensagem explicativa (fluxo 3a do UC-005)

**Critérios de aceite:** CA-023.1 – CA-023.5 | INT-4, RN-009, RNF-004

---

# FASE 7 — CONFIGURAÇÕES

## F7.1 — Informações do usuário (RF-025)
**Prioridade:** 🔴 | **Tamanho:** G
**Depende de:** F5.1

**Subtarefas:**
- [ ] Exibir nome, e-mail, foto de perfil, cartão mascarado e endereço com CEP (CA-024.1/25.x)
- [ ] Editar nome e foto de perfil (CA-025.1)
- [ ] Alterar e-mail com validação de unicidade (CA-025.2)
- [ ] Cartão registrado: exibir mascarado (ex.: **** 1234); cadastrar e remover (CA-025.3)
- [ ] Armazenar cartão **tokenizado**; exibição apenas parcial (RNF-008)
- [ ] Endereço: logradouro, número, complemento (opcional), bairro, cidade, estado, **CEP** (CA-025.4)
- [ ] Validar formato do CEP: #####-### (na aplicação; armazenar dígitos)

**Critérios de aceite:** CA-025.1 – CA-025.4 | RNF-008

---

## F7.2 — Dark Mode (RF-026)
**Prioridade:** 🟡 | **Tamanho:** M
**Depende de:** F5.1

**Subtarefas:**
- [ ] Toggle claro/escuro em Configurações (CA-026.1)
- [ ] Tema claro: fundo `#FAF8F2`; tema escuro: escala escura derivada de `#0F194A` (CA-026.1 / CA-ID.3)
- [ ] Destaques em `#8CC3F5` nos dois temas
- [ ] Persistir `tema` no perfil do usuário; aplicar no login (CA-026.2)
- [ ] Todos os componentes respeitam o tema: sidebar, vitrines, formulários, modais (CA-026.3)

**Critérios de aceite:** CA-026.1 – CA-026.3 | CA-ID.3, RNF-013

---

## F7.3 — Idioma pt-BR / en (RF-027)
**Prioridade:** 🟡 | **Tamanho:** M
**Depende de:** F5.1

**Subtarefas:**
- [ ] Seletor de idioma em Configurações
- [ ] I18n de menus, botões, mensagens e textos institucionais, com troca **imediata** (CA-027.1)
- [ ] Persistir `idioma` no perfil (CA-027.2 / RNF-014)
- [ ] Definir estratégia de tradução (arquivos de chaves; catálogo de livros permanece no idioma original)

**Critérios de aceite:** CA-027.1 – CA-027.2 | RNF-014

---

## F7.4 — Região (estado/cidade) (RF-028)
**Prioridade:** 🟡 | **Tamanho:** M
**Depende de:** F5.1

**Subtarefas:**
- [ ] Seleção em dois níveis: UF → cidade filtrada (CA-028.1)
- [ ] Persistir `uf`/`cidade` no perfil
- [ ] Preparar uso futuro: disponibilidade regional e prazos logísticos (CA-028.2 — sem implementação adicional agora, mas campo persistido)

**Critérios de aceite:** CA-028.1 – CA-028.2

---

## F7.5 — Ajuda e suporte (RF-029)
**Prioridade:** 🟡 | **Tamanho:** M
**Depende de:** F5.1

**Subtarefas:**
- [ ] Tópicos mínimos (CA-029.1), cada um citando as regras vigentes (CA-029.2):
  - [ ] Como alugar um livro (RF-009/010)
  - [ ] Prazos de devolução e extensões (RN-001, RN-002)
  - [ ] Multas por atraso e dano (RN-004, RN-005)
  - [ ] "Lido" e Lista de desejos (RF-021/022)
  - [ ] Como avaliar livros (RN-009)
  - [ ] Como funcionam as sugestões (RN-010)
  - [ ] Configurações: dark mode, idioma, região (RF-026/027/028)
  - [ ] Informações da conta e cartão (RF-025)
- [ ] "Entrar em contato": formulário/e-mail registrando o pedido com o e-mail do usuário (CA-029.3)
- [ ] Persistir pedidos de suporte (tabela `CONTATO_SUPORTE` — nova tabela, migration adicional)

**Critérios de aceite:** CA-029.1 – CA-029.3

---

# FASE 8 — IDENTIDADE VISUAL E QUALIDADE TRANSVERSAL

## F8.1 — Identidade visual global (§1.5)
**Prioridade:** 🔴 | **Tamanho:** M
**Depende de:** telas existentes (aplicação contínua)

**Subtarefas:**
- [ ] Definir tokens de design: cores `#0F194A` (primária), `#8CC3F5` (secundária/destaques/hover/ações), `#FAF8F2` (fundo claro) (CA-ID.1)
- [ ] Derivadas de estado: hover, ativo, desabilitado — sempre a partir da paleta (CA-ID.1)
- [ ] Fonte **Montserrat** para títulos, cabeçalhos e nomes de livros (CA-ID.2)
- [ ] Fonte **Open Sans** para descrições, textos, botões e formulários (CA-ID.2)
- [ ] Dark mode com escala escura derivada de `#0F194A` (CA-ID.3 — integração com F7.2)

**Critérios de aceite:** CA-ID.1 – CA-ID.3 | RNF-012

---

## F8.2 — Performance (RNF-001/004)
**Prioridade:** 🟡 | **Tamanho:** M

**Subtarefas:**
- [ ] Testar busca e filtros da Prateleira com 10.000 livros: ≤ 2s (RNF-001)
- [ ] Otimizar com índices do §6.4; avaliar FULLTEXT em `LIVRO(titulo, autor)`
- [ ] Medir recálculo de avaliação média: ≤ 1s (RNF-004)
- [ ] Documentar resultados dos benchmarks

---

## F8.3 — Usabilidade e acessibilidade (RNF-009/010/011)
**Prioridade:** 🟡 | **Tamanho:** M

**Subtarefas:**
- [ ] Testes responsivos: desktop e mobile; sidebar adaptável (RNF-009)
- [ ] Revisão de mensagens de erro/sucesso: claras e específicas (RNF-010)
- [ ] Contraste adequado e fonte legível (RNF-011)

---

## F8.4 — Testes E2E dos fluxos críticos
**Prioridade:** 🔴 | **Tamanho:** G

**Subtarefas:**
- [ ] UC-001 Cadastro de usuário (incluindo fluxos 3a/3b)
- [ ] UC-002 Login (incluindo credenciais inválidas)
- [ ] UC-003 Aluguel (incluindo cópias esgotadas e usuário inelegível — 3a/5a)
- [ ] UC-004 Marcar como Lido / Lista de Desejos
- [ ] UC-005 Avaliar livro (incluindo livro não lido)
- [ ] UC-006 Extensão (incluindo contador = 2)
- [ ] UC-007 Devolução (incluindo erro de livro não emprestado)
- [ ] UC-008 Multa por dano

---

## F8.5 — Testes de concorrência e integridade
**Prioridade:** 🔴 | **Tamanho:** M

**Subtarefas:**
- [ ] Cenário: 2 usuários alugando a última cópia disponível → apenas 1 sucesso (INT-1)
- [ ] Cenário: criação de 4º exemplar simultânea → bloqueada (INT-2)
- [ ] Cenário: devolução concorrente do mesmo exemplar → idempotente (INT-3)
- [ ] Cenário: avaliação sem "Lido" → rejeitada (INT-4)
- [ ] Cenário: exclusão de título com exemplar alugado → bloqueada

---

# PLANEJAMENTO SUGERIDO

| Sprint | Entregas | Observações |
|--------|----------|-------------|
| **Sprint 1** | F0.1–F0.4, F1.1–F1.4, F2.1 | Fundação + autenticação + primeiro cadastro de livro |
| **Sprint 2** | F2.2–F2.5, F3.1–F3.3 | Acervo completo + núcleo de aluguel (maior risco — sprint protegida) |
| **Sprint 3** | F3.4–F3.5, F4.1–F4.4, F5.1–F5.3 | Extensão, jobs, notificações, navegação e filtros |
| **Sprint 4** | F5.4–F5.5, F6.1–F6.3, F7.1–F7.5 | Sugestões, coleções, avaliações e configurações |
| **Sprint 5** | F8.1–F8.5 | Polimento visual, performance, E2E e hardening |

## Mapa de dependências críticas

```
F0 (infra/banco) → F1 (auth) → F1.3 (perfis) → F2 (acervo) → F3.1 (vitrine) → F3.2 (aluguel)
                                    ↓                                        ↓
                              F5.1 (sidebar) ←----------------------------┘
                                    ↓
        F5.2/F5.3 (Início/Prateleira) → F5.4/F5.5 (Sugestões)
        F6.1/F6.2 (coleções) → F6.3 (avaliações)
        F7.x (configurações)
F3.2 → F4.1 (job de atrasos) → F4.3 (visualização de multas)
F3.3 → F4.2 (multa por dano)
F3.2 → F4.4 (notificações)
```

## Pontos que exigem decisão (não fechados na spec)

1. **Multa de atraso em execuções diárias do job:** upsert da multa acumulada ou nova multa por execução (impacta F4.1 e F4.3).
2. **Sobreposição manual de tamanho (CA-008.2):** como persistir (flag `tamanho_manual`?) para que edições futuras não recalculem por cima.
3. **Desmarcar "Lido" com avaliação existente (F6.1):** remover avaliação ou proibir desmarcação.
4. **Fluxo de pagamento de multas (CA-015.2):** a spec não detalha; definir mínimo viável.
5. **Texto institucional editável (CA-018.1):** onde persistir (tabela de configurações).
6. **Recálculo da média:** trigger vs. aplicação (RNF-004).
7. **Provedor de e-mail** para o canal de notificações.

---

*Documento gerado a partir da especificação Aurora v3.0 — ajuste tamanhos/sprints conforme a capacidade do time.*
