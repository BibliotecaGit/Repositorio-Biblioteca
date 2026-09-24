# Aurora — Backlog de Tarefas (derivado da Spec v2.1)

**Base:** Especificação Técnica v2.1 (2026-09-24) · **Total:** 49 tarefas em 10 épicos

Legenda de prioridade: **Alta** (bloqueia o núcleo) · **Média** · **Baixa**. Estimativas em dias (d).
Banco de dados: **MySQL 8.0** (InnoDB, utf8mb4) — design físico na spec §6.

## Roadmap sugerido (ordem de execução)

| Fase | Épicos | Entrega |
|------|--------|---------|
| 1 — Fundação | E1 (T-001 a T-004) | Projeto rodando, design system, tema e i18n |
| 2 — Núcleo | E2 (T-005 a T-008) + E3 (T-008a a T-014) | Banco MySQL, login/cadastro e acervo completo |
| 3 — Motor de aluguel | E4 (T-015 a T-025) + E5 (T-026, T-027) | Aluguel, fila (10), pendente, devolução, multas, notificações |
| 4 — Experiência do usuário | E6 (T-028 a T-032) + E7 (T-033 a T-037) + E8 (T-038) | Sidebar, Início, Prateleira, coleções, avaliações, sugestões |
| 5 — Configurações | E9 (T-039 a T-043) | Perfil, dark mode, idioma, região, ajuda |
| 6 — Qualidade e entrega | E10 (T-044 a T-048) | Testes, performance e deploy |

## Dependências críticas (caminho principal)

T-001 → T-002 → T-005 → T-006 → T-008 (DDL MySQL) → T-009 → T-015 → T-017 → T-008a (integridade) → T-018 → T-019 → T-020 → T-026 → T-028 → (T-029…T-043) → T-045 → T-048

---

## E1 — Fundação e Design System

### T-001 — Setup do projeto e ambientes  ·  Prioridade: Alta  ·  Est.: 2d
- **Descrição:** Criar repositório, estrutura de pastas (front/back), CI básico, ambientes dev/homolog e banco de dados.
- **Requisitos:** —
- **Depende de:** —
- **Critérios de pronto:** Repo com build passando no CI; banco acessível em dev.

### T-002 — Design system Aurora  ·  Prioridade: Alta  ·  Est.: 3d
- **Descrição:** Implementar tokens de cor (#0F194A, #8CC3F5, #FAF8F2), tipografia Montserrat (títulos) e Open Sans (texto), componentes base (botões, inputs, cards de livro, modal).
- **Requisitos:** 1.5; RNF-012
- **Depende de:** T-001
- **Critérios de pronto:** Componentes documentados e usáveis; tipografia e paleta aplicadas.

### T-003 — Tema claro/escuro (Dark Mode)  ·  Prioridade: Média  ·  Est.: 1d
- **Descrição:** Implementar alternância de tema com persistência no perfil (base para RF-029).
- **Requisitos:** RF-029; RNF-013
- **Depende de:** T-002
- **Critérios de pronto:** Toggle de tema funcional; cores escuras derivadas de #0F194A.

### T-004 — Internacionalização (pt-BR/en)  ·  Prioridade: Baixa  ·  Est.: 2d
- **Descrição:** Estrutura de i18n com os dois idiomas (base para RF-030).
- **Requisitos:** RF-030; RNF-014
- **Depende de:** T-001
- **Critérios de pronto:** Chaves de tradução criadas; troca de idioma aplica-se às telas base.


## E2 — Autenticação e Sessão

### T-005 — Cadastro de usuário  ·  Prioridade: Alta  ·  Est.: 2d
- **Descrição:** Tela e API de cadastro com nome, e-mail e senha; validação de unicidade/formato; autologin e redirecionamento para a área logada (aba Início).
- **Requisitos:** RF-001; CA-001.1–1.5
- **Depende de:** T-001, T-002
- **Critérios de pronto:** Cadastro valida campos; e-mail duplicado rejeitado; após cadastro usuário cai na aba Início logado.

### T-006 — Login e logout  ·  Prioridade: Alta  ·  Est.: 2d
- **Descrição:** Login com e-mail + senha (mensagem genérica de erro); sessão com expiração de 30 min; item Sair da sidebar encerra sessão e redireciona ao login.
- **Requisitos:** RF-002, RF-033; CA-002.1–2.4, CA-033.1–3.3
- **Depende de:** T-005
- **Critérios de pronto:** Login/logout funcionais; sessão expira; rotas protegidas redirecionam ao login.

### T-007 — Perfis de acesso  ·  Prioridade: Alta  ·  Est.: 1d
- **Descrição:** Tipos de usuário (aluno, bibliotecário, administrador) com permissões diferenciadas.
- **Requisitos:** RF-003
- **Depende de:** T-006
- **Critérios de pronto:** Aluno sem acesso a gestão de acervo; bibliotecário/admin com acessos próprios.

### T-008 — Banco de dados — DDL MySQL das 11 tabelas  ·  Prioridade: Alta  ·  Est.: 2d
- **Descrição:** Criar USUARIO, LIVRO, EXEMPLAR, ALUGUEL, FILA_ESPERA, LISTA_PENDENTES, JA_LIDOS, LISTA_DESEJOS, AVALIACAO, USO_FILTRO e MULTA no MySQL 8.0 (InnoDB, utf8mb4) conforme spec §6.3: PKs BIGINT UNSIGNED AUTO_INCREMENT, enums de status, CHECKs (nota 0–5, posição 1–10, extensão ≤2, valores ≥0), FKs (RESTRICT no núcleo, CASCADE nas coleções) e setup de migrations versionadas reversíveis.
- **Requisitos:** Seção 6 (§6.1–6.5)
- **Depende de:** T-001
- **Critérios de pronto:** DDL executável criado; migrations reversíveis; índices da §6.4 aplicados; seed de 1 admin, 1 bibliotecário e 1 aluno.


## E3 — Acervo (Livros e Exemplares)

### T-008a — Integridade transacional (INT-1 a INT-7)  ·  Prioridade: Alta  ·  Est.: 2d
- **Descrição:** Implementar na API as travas das regras da spec §6.5: transações com SELECT ... FOR UPDATE em aluguel/fila/pendente/devolução; limite de 3 exemplares por livro; fila limitada a 10; um registro ativo por usuário/livro em fila e pendentes (coluna gerada + UNIQUE); avaliação condicionada a JA_LIDOS; soft delete com filtro deleted_at IS NULL em todo o catálogo.
- **Requisitos:** §6.5 (INT-1 a INT-7)
- **Depende de:** T-008, T-017
- **Critérios de pronto:** Testes concorrentes passam (ex.: dois usuários disputando a última cópia); duplicados em fila/pendentes rejeitados; catálogo nunca exibe deleted_at.

### T-009 — Cadastro de livro com 4 faixas de tamanho  ·  Prioridade: Alta  ·  Est.: 3d
- **Descrição:** API e tela de cadastro: título, autor, gênero, páginas, valor do livro, preço do aluguel, capa, data de lançamento; faixa de tamanho calculada automaticamente (≤100 / 100–150 / 150–250 / >250); 3 cópias por padrão.
- **Requisitos:** RF-004; CA-004.1–4.5; RN-001
- **Depende de:** T-007, T-008
- **Critérios de pronto:** Livro salvo com faixa correta; 3 exemplares gerados; campos validados.

### T-010 — CRUD de exemplares  ·  Prioridade: Alta  ·  Est.: 1d
- **Descrição:** Vínculo de exemplares ao livro (1–3), código de barras único opcional, status inicial 'disponível'.
- **Requisitos:** RF-005; CA-005.1–5.4
- **Depende de:** T-009
- **Critérios de pronto:** Exemplar criado/atualizado; validação de limite de 3 e unicidade de código.

### T-011 — Edição de livro  ·  Prioridade: Média  ·  Est.: 1d
- **Descrição:** Edição de informações com recálculo de faixa de tamanho e atualização do preço do aluguel no hover.
- **Requisitos:** RF-006; CA-006.1–6.3
- **Depende de:** T-009
- **Critérios de pronto:** Edição recalcula tamanho e propaga preço.

### T-012 — Exclusão lógica de título  ·  Prioridade: Média  ·  Est.: 1d
- **Descrição:** Exclusão bloqueada se houver aluguéis ativos, fila ou pendentes; soft delete com confirmação.
- **Requisitos:** RF-007; CA-007.1–7.3
- **Depende de:** T-009
- **Critérios de pronto:** Bloqueios aplicados; exclusão lógica registrada.

### T-013 — Categorização por admin  ·  Prioridade: Média  ·  Est.: 1d
- **Descrição:** Edição de gênero e tamanho (override) pelo administrador; flag infantil.
- **Requisitos:** RF-008; CA-008.1–8.4
- **Depende de:** T-011
- **Critérios de pronto:** Admin sobrescreve categorias; livro infantil aparece na aba Infantis.

### T-014 — Seed de catálogo  ·  Prioridade: Média  ·  Est.: 1d
- **Descrição:** Popular acervo com gêneros principais (romance, drama, ficção científica, fantasia, suspense, terror, autoajuda, biografia, história, poesia, infantil), autores de destaque e livros de exemplo com capas, páginas e datas de lançamento (incluindo lançamentos < 6 meses para Novidades).
- **Requisitos:** RF-021, RF-022; CA-021.2, CA-022.2–2.3
- **Depende de:** T-009
- **Critérios de pronto:** Catálogo com dados suficientes para todos os filtros da Prateleira.


## E4 — Aluguel, Fila, Pendente e Devolução

### T-015 — Vitrine de capas com hover e 3 botões  ·  Prioridade: Alta  ·  Est.: 3d
- **Descrição:** Grid de capas lado a lado; hover revela nome, preço do aluguel e avaliação; clique abre modal com Alugar / Entrar na fila / Pendente conforme estado (cópias, fila, fila cheia).
- **Requisitos:** RF-009; CA-009.1–9.4; RNF-015
- **Depende de:** T-002, T-014
- **Critérios de pronto:** Hover exibe as 3 infos; estados dos botões corretos em cada cenário.

### T-016 — Busca por título, autor ou gênero  ·  Prioridade: Alta  ·  Est.: 1d
- **Descrição:** Busca com resultados parciais na vitrine (FULLTEXT em titulo/autor quando escalar).
- **Requisitos:** RF-009; CA-009.4; §6.4
- **Depende de:** T-015
- **Critérios de pronto:** Busca retorna parciais em ≤2s (RNF-001).

### T-017 — Motor de aluguel  ·  Prioridade: Alta  ·  Est.: 3d
- **Descrição:** Aluguel de cópia disponível sem fila: elegibilidade (sem multa, não bloqueado, <3 livros), seleção automática de exemplar, cobrança do preço do aluguel, prazo 15/30 dias, notificação.
- **Requisitos:** RF-010; CA-010.1–10.10; RN-001, RN-008
- **Depende de:** T-008, T-015
- **Critérios de pronto:** Aluguel cria registro, muda exemplar para 'alugado', calcula prazo e notifica.

### T-018 — Fila de espera (máx. 10)  ·  Prioridade: Alta  ·  Est.: 3d
- **Descrição:** Entrada na fila quando 3 cópias alugadas ou fila existente; posição por chegada; validações de elegibilidade; desistência.
- **Requisitos:** RF-011, RF-015; CA-011.1–11.9; RN-003
- **Depende de:** T-017
- **Critérios de pronto:** Fila limitada a 10; posição FIFO; desistência notifica próximo.

### T-019 — Lista de Pendentes com promoção automática  ·  Prioridade: Alta  ·  Est.: 2d
- **Descrição:** Registro 'Pendente' quando fila cheia; promoção FIFO à fila quando abrir vaga (desistência, expiração, confirmação); cancelamento de pendente; notificação de promoção.
- **Requisitos:** RF-011A; CA-011A.1–6; RN-003, RN-007
- **Depende de:** T-018
- **Critérios de pronto:** Pendente promovido automaticamente e notificado; cancelamento reordena pendentes.

### T-020 — Devolução de livro  ·  Prioridade: Alta  ·  Est.: 2d
- **Descrição:** Fluxo de devolução com cálculo de multa (R$ 3/dia, teto = valor do livro), notificação do primeiro da fila, status 'notificado' com limite de 3 dias, disponibilização da cópia quando sem fila.
- **Requisitos:** RF-012; CA-012.1–12.8
- **Depende de:** T-017
- **Critérios de pronto:** Devolução gera multa se atraso; fila avança corretamente; cópia volta a 'disponível'.

### T-021 — Confirmação de aluguel pela fila  ·  Prioridade: Alta  ·  Est.: 2d
- **Descrição:** Tela/botão 'Confirmar aluguel' em 3 dias; expiração notifica próximo e promove pendente; desistência idem.
- **Requisitos:** RF-013; CA-013.1–13.6
- **Depende de:** T-018, T-020
- **Critérios de pronto:** Confirmação cria aluguel cobrando preço; expiração/desistência escalonam fila e pendentes.

### T-022 — Extensão de prazo (máx. 2)  ·  Prioridade: Média  ·  Est.: 2d
- **Descrição:** Solicitação de extensão (+2 meses, depois +1 mês) bloqueada com fila; devolução obrigatória em 3 dias se alguém entrar na fila.
- **Requisitos:** RF-014; CA-014.1–14.8; RN-002
- **Depende de:** T-017
- **Critérios de pronto:** Regras de contador e bloqueio aplicadas; notificações de obrigatoriedade.

### T-023 — Job diário de atrasos  ·  Prioridade: Alta  ·  Est.: 2d
- **Descrição:** Job 00:00 (RNF-003) sobre ALUGUEL.data_devolucao_prevista (índice da §6.4): detecta atrasos, calcula multas (teto valor_livro), aplica bloqueios (1 semana após 15 dias; +1 dia/dia após teto) e notifica.
- **Requisitos:** RF-016; CA-016.1–16.7; RN-004; RNF-003
- **Depende de:** T-017
- **Critérios de pronto:** Job executa diariamente; multas e bloqueios corretos; usuário penalizado não aluga/entra em fila/fica pendente.

### T-024 — Multa por dano  ·  Prioridade: Média  ·  Est.: 1d
- **Descrição:** Bibliotecário aplica multa fixa de R$ 20 com tipo de dano; exemplar 'danificado'; histórico de danos.
- **Requisitos:** RF-017; CA-017.1–17.5; RN-005
- **Depende de:** T-020
- **Critérios de pronto:** Multa cumulativa registrada; exemplar marcado; histórico por exemplar.

### T-025 — Visualização de multas pelo usuário  ·  Prioridade: Média  ·  Est.: 1d
- **Descrição:** Tela de multas pendentes e histórico (tipo, valor, dias de atraso, status, data).
- **Requisitos:** RF-018; CA-018.1–18.2
- **Depende de:** T-023, T-024
- **Critérios de pronto:** Usuário vê multas com status correto.


## E5 — Notificações

### T-026 — Serviço de notificações (e-mail + in-app)  ·  Prioridade: Alta  ·  Est.: 3d
- **Descrição:** Canal duplo para todos os eventos: chamada na fila, prazo próximo (3 dias antes), atraso, entrada na fila durante extensão, devolução obrigatória, promoção pendente→fila, confirmação de aluguel, multas.
- **Requisitos:** RF-019; CA-019.1–19.7; RN-007
- **Depende de:** T-017
- **Critérios de pronto:** Todos os eventos disparam e-mail e notificação in-app; central de notificações no app.

### T-027 — Central de notificações na interface  ·  Prioridade: Média  ·  Est.: 1d
- **Descrição:** Ícone/aba com histórico de notificações do usuário.
- **Requisitos:** RF-019
- **Depende de:** T-026
- **Critérios de pronto:** Usuário visualiza e marca como lida.


## E6 — Navegação, Início e Prateleira

### T-028 — Sidebar da área logada  ·  Prioridade: Alta  ·  Est.: 2d
- **Descrição:** Sidebar com Início, Prateleira, Sugestões, Já lidos, Lista de desejos, Avaliar livros, Configurações, Sair; item ativo destacado em #8CC3F5; responsiva.
- **Requisitos:** RF-020; CA-020.1–20.3; RNF-009
- **Depende de:** T-006, T-002
- **Critérios de pronto:** Todos os itens navegáveis e funcionais; estado ativo visível; colapsa em mobile.

### T-029 — Aba Início  ·  Prioridade: Alta  ·  Est.: 2d
- **Descrição:** Descrição da biblioteca Aurora (texto institucional editável pelo admin) e seção 'Novidades' com livros dos últimos 6 meses (capa, hover e clique conforme RF-009).
- **Requisitos:** RF-021; CA-021.1–21.3
- **Depende de:** T-015, T-028
- **Critérios de pronto:** Início exibe descrição e Novidades; fallback amigável quando sem lançamentos.

### T-030 — Prateleira — filtros Tamanho, Gênero e Autor  ·  Prioridade: Alta  ·  Est.: 3d
- **Descrição:** Aba Prateleira com 'Escolher filtros:'; tamanho nas 4 faixas; gêneros e autores do acervo; filtros combináveis.
- **Requisitos:** RF-022; CA-022.1–2.3, 2.7–2.8
- **Depende de:** T-015, T-028
- **Critérios de pronto:** Filtros retornam resultados corretos e combináveis.

### T-031 — Prateleira — Novidades, Destaques e Infantis  ·  Prioridade: Alta  ·  Est.: 2d
- **Descrição:** Filtros de lançamentos (<6 meses), destaques (muito alugados no ano + nota ≥4,5) e infantil.
- **Requisitos:** RF-022; CA-022.4–2.6; RN-011
- **Depende de:** T-030
- **Critérios de pronto:** Três seções populadas conforme regras.

### T-032 — Registro de uso de filtros  ·  Prioridade: Média  ·  Est.: 1d
- **Descrição:** Contabilizar uso de tamanho/gênero/autor por usuário (USO_FILTRO) para alimentar Sugestões.
- **Requisitos:** RF-023; CA-023.1; RN-010
- **Depende de:** T-030
- **Critérios de pronto:** Contadores incrementam a cada filtro aplicado.


## E7 — Coleções e Avaliações

### T-033 — Marcar como Lido  ·  Prioridade: Alta  ·  Est.: 2d
- **Descrição:** Ação 'Marcar como Lido' na vitrine/modal; aba Já lidos lista os livros com capa, autor e avaliação do usuário; remover marcação.
- **Requisitos:** RF-024; CA-024.1–24.4
- **Depende de:** T-015, T-028
- **Critérios de pronto:** Marcação persiste; aba Já lidos atualizada; só 'Lido' pode ser avaliado.

### T-034 — Lista de Desejos  ·  Prioridade: Alta  ·  Est.: 2d
- **Descrição:** Ação 'Adicionar à Lista de Desejos'; aba com capa, nome, autor, preço e avaliação; remover item.
- **Requisitos:** RF-025; CA-025.1–25.3
- **Depende de:** T-015, T-028
- **Critérios de pronto:** Lista funcional com adição/remoção.

### T-035 — Avaliação 0–5 estrelas  ·  Prioridade: Alta  ·  Est.: 2d
- **Descrição:** Avaliar apenas livros lidos; editar avaliação; recalcular avaliacao_media/total_avaliacoes do livro (denormalização §6.6).
- **Requisitos:** RF-026; CA-026.1–26.5; RN-009; RNF-004
- **Depende de:** T-033
- **Critérios de pronto:** Nota salva; média recalculada em <1s; livro não lido bloqueado para avaliação.

### T-036 — Aba Avaliar livros  ·  Prioridade: Média  ·  Est.: 1d
- **Descrição:** Visão com: lidos aguardando avaliação, avaliações feitas e médias de livros não lidos (leitura).
- **Requisitos:** RF-026; CA-026.4–26.5
- **Depende de:** T-035
- **Critérios de pronto:** Três seções exibidas corretamente.

### T-037 — Cálculo de Destaques com avaliações  ·  Prioridade: Média  ·  Est.: 1d
- **Descrição:** Manter avaliacao_media e alugueis_ultimo_ano coerentes (§6.6, com job mensal decaindo registros > 1 ano); destaques = nota ≥4,5 e volume de aluguéis (RN-011).
- **Requisitos:** RN-009, RN-011
- **Depende de:** T-031, T-035
- **Critérios de pronto:** Destaques refletem nota ≥4,5 e volume de aluguéis.


## E8 — Sugestões Personalizadas

### T-038 — Motor de sugestões  ·  Prioridade: Média  ·  Est.: 2d
- **Descrição:** Aba Sugestões priorizando gênero > autor > tamanho pelos mais usados (USO_FILTRO); reduzir prioridade de livros alugados ativos ou lidos; exibir motivo da sugestão.
- **Requisitos:** RF-023; CA-023.2–23.4; RN-010
- **Depende de:** T-032, T-033
- **Critérios de pronto:** Sugestões coerentes com histórico do usuário, com explicação visível.


## E9 — Configurações

### T-039 — Informações do usuário  ·  Prioridade: Alta  ·  Est.: 3d
- **Descrição:** Edição de nome, foto, e-mail (com validação de unicidade), cartão mascarado (cadastro/remoção, tokenização) e endereço com CEP validado.
- **Requisitos:** RF-028; CA-028.1–28.4; RNF-008
- **Depende de:** T-008, T-028
- **Critérios de pronto:** Dados editáveis; cartão só exibido mascarado; CEP validado.

### T-040 — Dark Mode nas configurações  ·  Prioridade: Média  ·  Est.: 0.5d
- **Descrição:** Toggle na tela de Configurações integrado ao tema global (usa T-003).
- **Requisitos:** RF-029; CA-029.1–29.3
- **Depende de:** T-003, T-028
- **Critérios de pronto:** Toggle persiste no perfil e aplica a todos os componentes.

### T-041 — Idioma nas configurações  ·  Prioridade: Média  ·  Est.: 1d
- **Descrição:** Seletor pt-BR/en aplicando i18n global (usa T-004).
- **Requisitos:** RF-030; CA-030.1–30.2
- **Depende de:** T-004, T-028
- **Critérios de pronto:** Troca imediata e persistida.

### T-042 — Região (estado e cidade)  ·  Prioridade: Média  ·  Est.: 1d
- **Descrição:** Seleção em dois níveis: UF → cidade, persistida no perfil.
- **Requisitos:** RF-031; CA-031.1–31.2
- **Depende de:** T-039
- **Critérios de pronto:** Cascata UF/cidade funcional com dados do Brasil.

### T-043 — Ajuda e suporte  ·  Prioridade: Média  ·  Est.: 2d
- **Descrição:** Tópicos explicando cada funcionalidade (aluguel, fila, pendente, prazos/extensões, multas, já lidos/desejos, avaliações, sugestões, configurações, conta/cartão) e formulário de contato registrando pedidos.
- **Requisitos:** RF-032; CA-032.1–32.3
- **Depende de:** T-028
- **Critérios de pronto:** Todos os tópicos presentes; contato registra e confirma envio.


## E10 — Qualidade e Entrega

### T-044 — Testes unitários do núcleo  ·  Prioridade: Alta  ·  Est.: 3d
- **Descrição:** Cobrir regras críticas: faixas de tamanho, elegibilidade de aluguel, fila (10), promoção de pendente, multas/teto/bloqueio, extensões, média de avaliações.
- **Requisitos:** RN-001 a RN-012
- **Depende de:** T-017 a T-023, T-035
- **Critérios de pronto:** Cobertura dos cenários de exceção das regras de negócio.

### T-045 — Testes E2E dos fluxos principais  ·  Prioridade: Alta  ·  Est.: 3d
- **Descrição:** Fluxos: cadastro→login→alugar; esgotar 3 cópias→fila; encher fila (10)→pendente→promoção; devolução com multa; avaliar livro lido; dark mode/idioma.
- **Requisitos:** Todos os RF
- **Depende de:** T-044
- **Critérios de pronto:** Cenários E2E passando no CI.

### T-046 — Responsividade e acessibilidade  ·  Prioridade: Média  ·  Est.: 2d
- **Descrição:** Revisão mobile/desktop, contraste da paleta, legibilidade das fontes e navegação por teclado/leitores.
- **Requisitos:** RNF-009, RNF-010, RNF-011
- **Depende de:** T-028
- **Critérios de pronto:** Sem bloqueios de acessibilidade nos fluxos principais.

### T-047 — Performance e observabilidade  ·  Prioridade: Média  ·  Est.: 2d
- **Descrição:** Validar busca e filtros ≤2s com 10 mil livros no MySQL (FULLTEXT conforme §6.4); logs e alertas do job diário.
- **Requisitos:** RNF-001 a RNF-004
- **Depende de:** T-016, T-023
- **Critérios de pronto:** Teste de carga da busca aprovado; alertas do job configurados.

### T-048 — Deploy e release v2.0  ·  Prioridade: Alta  ·  Est.: 1d
- **Descrição:** Publicação em produção, seeds finais, variáveis de ambiente e rollback.
- **Requisitos:** —
- **Depende de:** T-045, T-046, T-047
- **Critérios de pronto:** Aurora v2.0 no ar com monitoramento.

---

## Definition of Done (global)

- Código revisado e mergeado na branch principal com CI verde;
- Critérios de aceitação da spec (CA) correspondentes atendidos;
- Regras de negócio (RN) associadas cobertas por testes;
- **Banco:** toda mudança estrutural entrega migration versionada reversível para MySQL 8.0, fiel à spec §6.3–§6.5;
- Interface fiel à identidade Aurora (paleta, Montserrat/Open Sans, RNF-012);
- Mensagens de erro/sucesso claras e traduzidas (pt-BR/en quando o módulo de i18n estiver ativo);
- Tarefas de backend entregam API + migração + seed de teste.

*Arquivo complementar: `tarefas.csv` (lista plana para importar em Jira/Trello/GitHub Projects).*
