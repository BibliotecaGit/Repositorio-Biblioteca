# Especificação Técnica — Aurora: Sistema de Biblioteca com Aluguel de Livros

**Versão:** 2.1
**Data:** 2026-09-24
**Status:** Aprovado para desenvolvimento
**Autor:** Equipe de Análise Spec-Driven

---

## 1. Introdução

### 1.1 Propósito

Esta especificação define os requisitos funcionais, regras de negócio, casos de uso, critérios de aceitação e requisitos de interface para o desenvolvimento do **Aurora**, um sistema de biblioteca com aluguel de livros, fila de espera, lista de pendentes, multas por atraso, extensão de prazos, avaliações e recomendações personalizadas.

### 1.2 Escopo

O Aurora será utilizado por alunos, bibliotecários e administradores para gerenciar o acervo, controlar empréstimos (aluguéis), gerenciar filas de espera e listas de pendentes, aplicar penalidades, avaliar livros e personalizar a experiência do usuário (tema, idioma e região). A versão 2.0 incorpora os requisitos de interface da área logada (sidebar, prateleira com filtros, sugestões, coleções do usuário e configurações).

### 1.3 Definições e Acrônimos

| Termo | Definição |
|-------|-----------|
| Aluguel | Empréstimo de um exemplar físico de livro para um usuário cadastrado |
| Exemplar (cópia) | Unidade física de um livro. Cada título possui **3 cópias** por padrão |
| Fila de Espera | Lista ordenada (máx. **10 pessoas**) de usuários aguardando a disponibilidade de um livro |
| Pendente | Estado do usuário que manifestou interesse quando a fila estava cheia; é promovido à fila quando abre vaga |
| Extensão | Prolongamento do prazo de devolução mediante solicitação do usuário |
| Multa | Valor monetário aplicado por atraso ou dano ao exemplar |
| Bloqueio | Período em que o usuário fica impedido de realizar novos aluguéis |
| Já Lidos | Coleção pessoal de livros marcados como "Lido" pelo usuário |
| Lista de Desejos | Coleção pessoal de livros marcados como "Adicionar à Lista de Desejos" |
| Dark Mode | Tema escuro opcional da interface |
| Novidades | Livros lançados nos últimos 6 meses |
| Destaques | Livros muito alugados no último ano e com avaliação média alta |

### 1.4 Referências

- Análise Spec-Driven — Rodadas 1, 2 e 3
- Documento de Regras de Negócio Consolidadas
- Modelagem Técnica (Casos de Uso, MER, Máquina de Estados)
- Briefing de Produto Aurora v2.0 (requisitos de interface e aluguel)

### 1.5 Identidade Visual e Tipografia

| Item | Valor |
|------|-------|
| Nome do site | **Aurora** |
| Cor primária (fundo escuro / detalhes) | `#0F194A` |
| Cor secundária (destaques, hover, ações) | `#8CC3F5` |
| Cor de fundo (tema claro) | `#FAF8F2` |
| Fonte de títulos | **Montserrat** |
| Fonte de texto corrido | **Open Sans** |

**Critérios de Aceitação:**
- [CA-ID.1] Todos os elementos visuais da aplicação devem usar exclusivamente a paleta definida acima (incluindo estados hover, ativo e desabilitado derivados dessas cores).
- [CA-ID.2] Títulos, cabeçalhos de seção e nomes de livros usam Montserrat; descrições, textos corridos, botões e formulários usam Open Sans.
- [CA-ID.3] O Dark Mode substitui o fundo claro (`#FAF8F2`) por uma escala escura derivada de `#0F194A`, mantendo `#8CC3F5` para destaques.

---

## 2. Requisitos Funcionais

### 2.1 Módulo de Autenticação e Cadastro

#### RF-001 — Cadastro de Usuário
**Prioridade:** Alta
**Ator:** Usuário (aluno)
**Descrição:** O sistema deve permitir que novos usuários se cadastrem informando **nome, e-mail e senha**. Após o cadastro, o usuário é redirecionado automaticamente para a área logada (aba principal com sidebar).

**Critérios de Aceitação:**
- [CA-001.1] Os campos nome, e-mail e senha são obrigatórios.
- [CA-001.2] O e-mail deve ser validado quanto à unicidade e ao formato.
- [CA-001.3] A senha deve ter no mínimo 6 caracteres.
- [CA-001.4] O tipo de usuário padrão no cadastro é "aluno".
- [CA-001.5] Após cadastro bem-sucedido, o usuário é autenticado automaticamente e redirecionado para a aba **Início** da área logada.

#### RF-002 — Login
**Prioridade:** Alta
**Ator:** Usuário, Bibliotecário, Administrador
**Descrição:** O sistema deve permitir login utilizando **e-mail e senha**.

**Critérios de Aceitação:**
- [CA-002.1] O login é obrigatório para acessar a área logada e para todas as operações de aluguel, fila, pendente, avaliação e coleções.
- [CA-002.2] Credenciais inválidas devem retornar mensagem genérica de erro (não revelar qual campo está incorreto).
- [CA-002.3] Após o login, o usuário é redirecionado para a aba **Início**.
- [CA-002.4] Sessão deve expirar após 30 minutos de inatividade.

#### RF-003 — Controle de Acesso por Perfil
**Prioridade:** Alta
**Ator:** Sistema
**Descrição:** O sistema deve diferenciar permissões com base no tipo de usuário (aluno, bibliotecário, administrador).

**Critérios de Aceitação:**
- [CA-003.1] Alunos têm acesso à sidebar completa (Início, Prateleira, Sugestões, Já lidos, Lista de desejos, Avaliar livros, Configurações, Sair) e às operações de aluguel, fila, pendente e visualização de histórico.
- [CA-003.2] Bibliotecários podem gerenciar o acervo (livros e exemplares) e aplicar multas por dano.
- [CA-003.3] Administradores podem categorizar títulos (gênero, tamanho, autor) e excluir títulos do sistema.

---

### 2.2 Módulo de Acervo

#### RF-004 — Cadastro de Livro
**Prioridade:** Alta
**Ator:** Bibliotecário
**Descrição:** O sistema deve permitir o cadastro de novos títulos com título, autor, gênero, número de páginas, valor do livro, **preço do aluguel**, **capa** e **data de lançamento**.

**Critérios de Aceitação:**
- [CA-004.1] Todos os campos são obrigatórios, exceto a data de lançamento (quando ausente, o livro não aparece em "Novidades").
- [CA-004.2] O campo "tamanho" é calculado automaticamente em **4 faixas** (ver RN-001): Pequeno (até 100 páginas), Médio-pequeno (100 a 150), Médio-padrão (150 a 250), Grande (mais de 250).
- [CA-004.3] O campo "gênero" é preenchido pelo bibliotecário, mas pode ser reorganizado pelo administrador.
- [CA-004.4] O valor do livro é utilizado como teto para multas de atraso; o **preço do aluguel** é exibido ao usuário no hover da capa.
- [CA-004.5] Cada título nasce com **3 cópias (exemplares)** disponíveis por padrão.

#### RF-005 — Cadastro de Exemplar
**Prioridade:** Alta
**Ator:** Bibliotecário
**Descrição:** O sistema deve permitir o cadastro de exemplares físicos vinculados a um livro, com código de barras opcional. **Cada título possui no máximo 3 exemplares.**

**Critérios de Aceitação:**
- [CA-005.1] Cada exemplar deve estar vinculado a um livro existente.
- [CA-005.2] O código de barras, quando informado, deve ser único.
- [CA-005.3] O status inicial do exemplar é "disponível".
- [CA-005.4] Um livro pode ter de 1 a 3 exemplares (padrão: 3).

#### RF-006 — Edição de Livro
**Prioridade:** Média
**Ator:** Bibliotecário
**Descrição:** O sistema deve permitir a edição das informações de um livro já cadastrado.

**Critérios de Aceitação:**
- [CA-006.1] Alteração no número de páginas deve recalcular a faixa de tamanho automaticamente.
- [CA-006.2] Alteração no valor do livro deve refletir no teto de multas futuras (multas já geradas não são alteradas).
- [CA-006.3] Alteração no preço do aluguel reflete imediatamente no hover da capa.

#### RF-007 — Exclusão de Título
**Prioridade:** Média
**Ator:** Administrador
**Descrição:** O sistema deve permitir que o administrador exclua títulos do acervo.

**Critérios de Aceitação:**
- [CA-007.1] Não é permitido excluir um livro que possua exemplares atualmente alugados.
- [CA-007.2] Não é permitido excluir um livro que possua usuários na fila de espera ou na lista de pendentes.
- [CA-007.3] A exclusão deve ser lógica (soft delete) ou com confirmação explícita.

#### RF-008 — Categorização de Títulos
**Prioridade:** Média
**Ator:** Administrador
**Descrição:** O sistema deve permitir que o administrador organize os títulos por gênero, tamanho e autor.

**Critérios de Aceitação:**
- [CA-008.1] O administrador pode editar o gênero de qualquer livro.
- [CA-008.2] O administrador pode alterar a classificação de tamanho manualmente, sobrescrevendo a regra automática.
- [CA-008.3] O gênero "Infantil" habilita o livro na aba **Infantis** da Prateleira.
- [CA-008.4] Mudanças de categoria não afetam aluguéis em andamento.

---

### 2.3 Módulo de Aluguel, Fila e Pendente

#### RF-009 — Vitrine de Livros (Hover e Seleção)
**Prioridade:** Alta
**Ator:** Usuário
**Descrição:** Na página inicial, as **capas dos livros** devem ser exibidas lado a lado. Ao **passar o mouse (hover)** sobre uma capa, devem aparecer as informações: **nome do livro, preço do aluguel e avaliação (média de estrelas)**. Ao **clicar** no livro, devem aparecer **3 botões: "Alugar", "Entrar na fila" e "Pendente"**, habilitados conforme o estado do livro.

**Critérios de Aceitação:**
- [CA-009.1] A vitrine exibe capas lado a lado (layout responsivo).
- [CA-009.2] O hover revela: nome, preço do aluguel e avaliação média.
- [CA-009.3] O clique abre a ação com 3 botões, com o seguinte comportamento:
  - **"Alugar"** habilitado quando há ao menos 1 das 3 cópias disponíveis **e** não há fila; caso contrário, desabilitado.
  - **"Entrar na fila"** habilitado quando as 3 cópias estão alugadas **e** a fila tem menos de 10 pessoas; se não houver ninguém na fila (há cópias livres), o botão fica desabilitado (não se entra em fila de livro disponível).
  - **"Pendente"** habilitado quando a fila está cheia (10 pessoas); registra o interesse do usuário para promoção automática quando abrir vaga.
- [CA-009.4] A busca por título, autor ou gênero continua disponível na vitrine, retornando resultados parciais.

#### RF-010 — Aluguel de Livro Disponível
**Prioridade:** Alta
**Ator:** Usuário
**Descrição:** O sistema deve permitir alugar um livro que possua ao menos 1 de suas **3 cópias** disponíveis e não tenha fila de espera.

**Critérios de Aceitação:**
- [CA-010.1] O usuário deve estar autenticado.
- [CA-010.2] O usuário não pode ter atrasos/multas pendentes.
- [CA-010.3] O usuário não pode estar bloqueado.
- [CA-010.4] O usuário não pode ter 3 ou mais livros alugados simultaneamente.
- [CA-010.5] Se há fila de espera para o livro, "Alugar" não é exibido/habilitado; exibe-se "Entrar na fila".
- [CA-010.6] Se não há fila e há cópias disponíveis, "Alugar" é exibido.
- [CA-010.7] Ao alugar, o sistema seleciona automaticamente um exemplar disponível e cobra o **preço do aluguel**.
- [CA-010.8] A data de devolução prevista é calculada: hoje + 15 dias (até 150 páginas) ou + 30 dias (mais de 150 páginas).
- [CA-010.9] O status do exemplar muda para "alugado".
- [CA-010.10] Uma notificação de confirmação é enviada por e-mail e pelo sistema.

#### RF-011 — Entrada na Fila de Espera
**Prioridade:** Alta
**Ator:** Usuário
**Descrição:** O sistema deve permitir entrar na fila quando **as 3 cópias estiverem alugadas** ou quando já existir fila. A fila tem **máximo de 10 pessoas**.

**Critérios de Aceitação:**
- [CA-011.1] O usuário deve estar autenticado.
- [CA-011.2] O usuário não pode ter atrasos/multas pendentes.
- [CA-011.3] O usuário não pode estar bloqueado.
- [CA-011.4] O usuário não pode ter 3 ou mais livros alugados.
- [CA-011.5] A fila não pode ter 10 ou mais pessoas.
- [CA-011.6] Se a fila está cheia (10 pessoas), o botão "Entrar na fila" é substituído pelo botão **"Pendente"**.
- [CA-011.7] A posição na fila é determinada por ordem de chegada (última posição + 1).
- [CA-011.8] O status na fila é "aguardando".
- [CA-011.9] O usuário pode desistir da fila a qualquer momento.

#### RF-011A — Lista de Pendentes
**Prioridade:** Alta
**Ator:** Usuário
**Descrição:** Quando a fila de um livro está cheia (10 pessoas), o usuário poderá marcar o livro como **"Pendente"**. Quando abrir uma vaga na fila (por desistência, expiração ou confirmação), o primeiro usuário da lista de pendentes é **promovido automaticamente à fila de espera**.

**Critérios de Aceitação:**
- [CA-011A.1] "Pendente" só é exibido quando a fila possui 10 pessoas.
- [CA-011A.2] Um usuário não pode ficar pendente duas vezes para o mesmo livro, nem pendente e na fila simultaneamente.
- [CA-011A.3] A promoção ocorre em ordem de chegada (FIFO).
- [CA-011A.4] Ao ser promovido, o usuário recebe notificação (e-mail + in-app) informando a posição na fila.
- [CA-011A.5] O usuário pode cancelar o pendente a qualquer momento.
- [CA-011A.6] Ao cancelar, o próximo pendente é promovido à fila quando houver nova vaga.

#### RF-012 — Devolução de Livro
**Prioridade:** Alta
**Ator:** Usuário, Bibliotecário
**Descrição:** O sistema deve permitir a devolução de um livro alugado.

**Critérios de Aceitação:**
- [CA-012.1] Se o livro não estiver emprestado para o usuário, o sistema deve alertar com mensagem de erro.
- [CA-012.2] Se houver atraso, o sistema calcula a multa automaticamente (R$ 3,00/dia, teto = valor do livro).
- [CA-012.3] Se houver fila de espera para o livro, o sistema notifica o primeiro da fila.
- [CA-012.4] A notificação é enviada por e-mail e pelo sistema.
- [CA-012.5] O status do primeiro da fila muda para "notificado".
- [CA-012.6] O prazo de resposta do notificado é de 3 dias (data_limite_resposta = hoje + 3).
- [CA-012.7] Se não houver fila, o exemplar muda para "disponível" imediatamente.
- [CA-012.8] Se múltiplas cópias forem devolvidas, cada uma notifica o próximo da fila individualmente (uma de cada vez).

#### RF-013 — Confirmação de Aluguel pela Fila
**Prioridade:** Alta
**Ator:** Usuário
**Descrição:** O sistema deve permitir que o usuário notificado na fila confirme o aluguel do livro.

**Critérios de Aceitação:**
- [CA-013.1] O usuário deve clicar no botão "Confirmar aluguel" dentro do prazo de 3 dias.
- [CA-013.2] Ao confirmar, o sistema cria um novo registro de ALUGUEL (cobrando o preço do aluguel).
- [CA-013.3] O exemplar muda para "alugado".
- [CA-013.4] O status na fila muda para "confirmado".
- [CA-013.5] Se o usuário não confirmar em 3 dias, o status muda para "expirado", o próximo da fila é notificado e, havendo vaga, o primeiro pendente é promovido.
- [CA-013.6] Se o usuário desistir, o status muda para "desistiu", o próximo da fila é notificado imediatamente e, havendo vaga, o primeiro pendente é promovido.

#### RF-014 — Solicitação de Extensão de Prazo
**Prioridade:** Alta
**Ator:** Usuário
**Descrição:** O sistema deve permitir que o usuário solicite a extensão do prazo de devolução.

**Critérios de Aceitação:**
- [CA-014.1] A extensão só é permitida se não houver ninguém na fila de espera para o livro.
- [CA-014.2] O contador de extensões deve ser menor que 2.
- [CA-014.3] 1ª extensão: data_devolucao = hoje + 2 meses; contador = 1.
- [CA-014.4] 2ª extensão: data_devolucao = hoje + 1 mês; contador = 2.
- [CA-014.5] A extensão requer ação explícita do usuário (não é automática).
- [CA-014.6] Se alguém entrar na fila durante o período de extensão, o usuário é notificado e tem 3 dias para devolver obrigatoriamente.
- [CA-014.7] Se o usuário não devolver em 3 dias após a notificação de entrada na fila, inicia-se multa de atraso normalmente.
- [CA-014.8] Após a 2ª extensão, a devolução é obrigatória e notificada no sistema.

#### RF-015 — Desistência da Fila ou do Pendente
**Prioridade:** Média
**Ator:** Usuário
**Descrição:** O sistema deve permitir que um usuário desista da fila de espera ou cancele seu registro de pendente.

**Critérios de Aceitação:**
- [CA-015.1] O usuário pode desistir a qualquer momento.
- [CA-015.2] Ao desistir da fila, o status muda para "desistiu" e o primeiro pendente é promovido à fila.
- [CA-015.3] Ao cancelar o pendente, o registro é removido e o próximo pendente ocupa sua posição na ordem de chegada.
- [CA-015.4] O livro não fica reservado para quem desistiu.

---

### 2.4 Módulo de Multas e Penalidades

#### RF-016 — Cálculo Automático de Multa por Atraso
**Prioridade:** Alta
**Ator:** Sistema
**Descrição:** O sistema deve calcular automaticamente a multa por atraso na devolução.

**Critérios de Aceitação:**
- [CA-016.1] Um job diário verifica todos os aluguéis com data_devolucao_prevista < hoje.
- [CA-016.2] dias_atraso = hoje - data_prevista.
- [CA-016.3] valor_multa = dias_atraso × R$ 3,00.
- [CA-016.4] Se valor_multa ≥ valor_livro, valor_multa = valor_livro (teto).
- [CA-016.5] Se dias_atraso > 15, o usuário é bloqueado por 1 semana (bloqueio_ate = hoje + 7 dias).
- [CA-016.6] Se o valor atingiu o teto e o atraso continua: +1 dia de bloqueio por cada dia adicional de atraso.
- [CA-016.7] O usuário fica impedido de alugar, entrar em fila ou ficar pendente enquanto houver multa pendente ou bloqueio ativo.

#### RF-017 — Aplicação de Multa por Dano
**Prioridade:** Média
**Ator:** Bibliotecário
**Descrição:** O sistema deve permitir que o bibliotecário aplique uma multa de R$ 20,00 por dano ao exemplar.

**Critérios de Aceitação:**
- [CA-017.1] A multa é aplicada manualmente pelo bibliotecário durante o processo de devolução física.
- [CA-017.2] O bibliotecário deve selecionar o tipo de dano observado.
- [CA-017.3] O valor é fixo em R$ 20,00 e não é editável pelo bibliotecário.
- [CA-017.4] A multa por dano é cumulativa com a multa por atraso.
- [CA-017.5] O sistema deve registrar o histórico de danos por exemplar.

#### RF-018 — Visualização de Multas
**Prioridade:** Média
**Ator:** Usuário
**Descrição:** O sistema deve permitir que o usuário visualize suas multas pendentes e histórico.

**Critérios de Aceitação:**
- [CA-018.1] O usuário deve visualizar: tipo de multa, valor, dias de atraso (se aplicável), status (pendente/pago) e data de geração.
- [CA-018.2] Multas pagas devem ser marcadas como "pago".

---

### 2.5 Módulo de Notificações

#### RF-019 — Notificações do Sistema
**Prioridade:** Alta
**Ator:** Sistema
**Descrição:** O sistema deve enviar notificações por e-mail e pelo próprio sistema (in-app) para eventos críticos.

**Critérios de Aceitação:**
- [CA-019.1] Notificação quando o usuário é chamado na fila (próximo da fila).
- [CA-019.2] Notificação quando o prazo de devolução está próximo (3 dias antes do vencimento).
- [CA-019.3] Notificação quando um atraso é detectado.
- [CA-019.4] Notificação quando alguém entra na fila durante a extensão (devolução obrigatória em 3 dias).
- [CA-019.5] Notificação quando a devolução é obrigatória após o máximo de extensões.
- [CA-019.6] Notificação quando o usuário pendente é **promovido à fila de espera**.
- [CA-019.7] Todas as notificações devem ser enviadas por e-mail e pelo sistema simultaneamente.

---

### 2.6 Módulo de Navegação e Descoberta (Interface da Área Logada)

#### RF-020 — Estrutura da Área Logada (Sidebar)
**Prioridade:** Alta
**Ator:** Usuário
**Descrição:** Após cadastro/login, o usuário é redirecionado para a aba principal, que contém uma **sidebar** com os itens: **Início, Prateleira, Sugestões, Já lidos, Lista de desejos, Avaliar livros, Configurações e Sair**. Todos os itens devem ser funcionais.

**Critérios de Aceitação:**
- [CA-020.1] A sidebar é exibida em todas as telas da área logada.
- [CA-020.2] Cada item redireciona para sua respectiva seção:
  - **Início** → RF-021;
  - **Prateleira** → RF-022;
  - **Sugestões** → RF-023;
  - **Já lidos** → RF-024;
  - **Lista de desejos** → RF-025;
  - **Avaliar livros** → RF-026;
  - **Configurações** → RF-027 a RF-031;
  - **Sair** → RF-032.
- [CA-020.3] O item ativo fica visualmente destacado (cor `#8CC3F5`).

#### RF-021 — Início
**Prioridade:** Alta
**Ator:** Usuário
**Descrição:** A aba Início deve conter uma breve **descrição sobre a biblioteca Aurora** (texto institucional editável pelo administrador) e uma seção **"Novidades"** com sugestões de livros lançados nos últimos 6 meses.

**Critérios de Aceitação:**
- [CA-021.1] A descrição da biblioteca é exibida no topo da página (conteúdo padrão: apresentação da Aurora como biblioteca comunitária de aluguel de livros, missão de incentivo à leitura e funcionamento do acervo com 3 cópias por título).
- [CA-021.2] A seção "Novidades" lista livros com `data_lancamento` dentro dos últimos 6 meses, exibindo capa, nome, preço do aluguel e avaliação (com hover, conforme RF-009).
- [CA-021.3] Se não houver lançamentos no período, exibe-se mensagem amigável e os livros mais recentes cadastrados.

#### RF-022 — Prateleira (Filtros)
**Prioridade:** Alta
**Ator:** Usuário
**Descrição:** A aba Prateleira deve exibir o texto **"Escolher filtros:"** e oferecer 6 formas de filtragem: **Por Tamanho, Por Gênero, Por Autor, Novidades, Destaques e Infantis**.

**Critérios de Aceitação:**
- [CA-022.1] **Por Tamanho** — 4 opções: Pequeno (até 100 páginas), Médio-pequeno (100 a 150), Médio-padrão (150 a 250), Grande (mais de 250 páginas).
- [CA-022.2] **Por Gênero** — lista dos principais gêneros mais lidos (romance, drama, ficção científica, fantasia, suspense/thriller, terror, autoajuda, biografia, história, poesia, infantil, entre outros), com os gêneros cadastrados no acervo.
- [CA-022.3] **Por Autor** — lista dos principais autores do acervo (ex.: J.K. Rowling, George R.R. Martin, Colleen Hoover, Agatha Christie, Machado de Assis, Clarice Lispector, Stephen King, Haruki Murakami, Rick Riordan, Jane Austen), ordenados por relevância.
- [CA-022.4] **Novidades** — livros lançados nos últimos 6 meses.
- [CA-022.5] **Destaques** — livros muito alugados no último ano **e** com avaliação média alta (≥ 4,5 estrelas).
- [CA-022.6] **Infantis** — livros com gênero "Infantil".
- [CA-022.7] Os filtros podem ser combinados; o uso de cada filtro é contabilizado para alimentar as Sugestões (RF-023).
- [CA-022.8] Os resultados exibem capas lado a lado com hover e clique conforme RF-009.

#### RF-023 — Sugestões
**Prioridade:** Média
**Ator:** Usuário
**Descrição:** A aba Sugestões deve juntar os filtros mais escolhidos e utilizados pelo usuário (tamanho, gênero e autor) para sugerir livros que ele possa alugar.

**Critérios de Aceitação:**
- [CA-023.1] O sistema registra a contagem de uso de cada filtro (tamanho, gênero, autor) por usuário.
- [CA-023.2] As sugestões são compostas priorizando: (1) gênero mais usado, (2) autor mais usado, (3) faixa de tamanho mais usada.
- [CA-023.3] Livros já alugados ativamente ou já marcados como "Lido" aparecem com menor prioridade.
- [CA-023.4] O usuário visualiza o motivo da sugestão (ex.: "Porque você lê muito Romance").

---

### 2.7 Módulo de Coleções e Avaliações do Usuário

#### RF-024 — Já Lidos
**Prioridade:** Alta
**Ator:** Usuário
**Descrição:** A aba Já lidos deve exibir os livros que o usuário marcar como **"Lido"**.

**Critérios de Aceitação:**
- [CA-024.1] Nas telas de catálogo e na ação do livro, deve existir a opção "Marcar como Lido".
- [CA-024.2] A aba Já lidos lista todos os livros marcados, com capa, nome, autor e avaliação do usuário (se houver).
- [CA-024.3] O usuário pode remover a marcação de "Lido".
- [CA-024.4] Apenas livros marcados como "Lido" podem ser avaliados (RF-026).

#### RF-025 — Lista de Desejos
**Prioridade:** Alta
**Ator:** Usuário
**Descrição:** A aba Lista de desejos deve exibir os livros que o usuário marcar como **"Adicionar à Lista de Desejos"**.

**Critérios de Aceitação:**
- [CA-025.1] Nas telas de catálogo e na ação do livro, deve existir a opção "Adicionar à Lista de Desejos".
- [CA-025.2] A aba Lista de desejos lista todos os livros marcados, com capa, nome, autor, preço do aluguel e avaliação.
- [CA-025.3] O usuário pode remover o livro da lista.

#### RF-026 — Avaliar Livros
**Prioridade:** Alta
**Ator:** Usuário
**Descrição:** Na aba **Avaliar livros**, o usuário poderá avaliar livros marcados como **"Já lidos"** com notas de **0 a 5 estrelas**, além de poder **ver avaliações de livros não lidos ou já lidos**.

**Critérios de Aceitação:**
- [CA-026.1] A avaliação (0–5 estrelas) só pode ser dada a livros marcados como "Lido" pelo usuário.
- [CA-026.2] O usuário pode alterar sua avaliação a qualquer momento.
- [CA-026.3] A avaliação média de cada livro (exibida no hover da capa e nas fichas) é recalculada automaticamente.
- [CA-026.4] Na aba Avaliar livros, o usuário visualiza: (a) livros lidos aguardando avaliação; (b) suas avaliações já realizadas; (c) avaliações médias de livros não lidos (somente leitura).
- [CA-026.5] Avaliações são anônimas para outros usuários (exibida apenas a média).

---

### 2.8 Módulo de Configurações

#### RF-027 — Configurações (Visão Geral)
**Prioridade:** Alta
**Ator:** Usuário
**Descrição:** A aba Configurações deve conter: **Informações do usuário, Dark Mode, Idioma, Região e Ajuda e suporte**.

**Critérios de Aceitação:**
- [CA-027.1] Todas as subseções são funcionais e acessíveis a partir da tela de Configurações.

#### RF-028 — Informações do Usuário
**Prioridade:** Alta
**Ator:** Usuário
**Descrição:** A subseção Informações do usuário deve conter: **nome do usuário, e-mail do usuário, cartão registrado, foto de perfil e endereço do usuário contendo CEP**.

**Critérios de Aceitação:**
- [CA-028.1] O usuário pode editar nome e foto de perfil.
- [CA-028.2] O e-mail é exibido e pode ser alterado mediante validação de unicidade.
- [CA-028.3] O cartão registrado exibe os dados mascarados (ex.: **** 1234) e permite cadastrar/remover cartão (usado para cobrança do aluguel e multas).
- [CA-028.4] O endereço contém: logradouro, número, complemento (opcional), bairro, cidade, estado e **CEP**; o CEP é validado quanto ao formato (#####-###).

#### RF-029 — Dark Mode
**Prioridade:** Média
**Ator:** Usuário
**Descrição:** O Dark Mode deve tornar a interface escura em vez da aparência clara padrão.

**Critérios de Aceitação:**
- [CA-029.1] Um toggle alterna entre tema claro (fundo `#FAF8F2`) e tema escuro (escala derivada de `#0F194A`).
- [CA-029.2] A preferência é salva no perfil do usuário e persistida entre sessões.
- [CA-029.3] Todos os componentes (sidebar, vitrines, formulários, modais) respeitam o tema ativo.

#### RF-030 — Idioma
**Prioridade:** Média
**Ator:** Usuário
**Descrição:** O usuário poderá escolher o idioma da interface entre **Português (pt-BR)** e **Inglês (en)**.

**Critérios de Aceitação:**
- [CA-030.1] A troca de idioma aplica-se imediatamente a menus, botões, mensagens e textos institucionais.
- [CA-030.2] A preferência é persistida no perfil.

#### RF-031 — Região
**Prioridade:** Média
**Ator:** Usuário
**Descrição:** O usuário poderá escolher a região em que vive, de acordo com **estado e cidade no Brasil**.

**Critérios de Aceitação:**
- [CA-031.1] Seleção em dois níveis: primeiro o estado (UF), depois a cidade (lista filtrada).
- [CA-031.2] A região pode ser usada para disponibilidade regional do acervo e cálculo de prazos logísticos futuros.

#### RF-032 — Ajuda e Suporte
**Prioridade:** Média
**Ator:** Usuário
**Descrição:** A subseção Ajuda e suporte deve conter tópicos explicando como cada funcionalidade do site funciona (de acordo com esta especificação) e uma opção de **entrar em contato**.

**Critérios de Aceitação:**
- [CA-032.1] Tópicos mínimos: como alugar um livro; como funciona a fila de espera; como funciona o "Pendente"; prazos de devolução e extensões; multas por atraso e dano; como marcar livros como "Lido" e usar a Lista de desejos; como avaliar livros; como funcionam as sugestões; como usar as configurações (dark mode, idioma, região); informações da conta e cartão.
- [CA-032.2] Cada tópico explica o passo a passo com referência às regras vigentes (RN-001 a RN-012).
- [CA-032.3] A opção "Entrar em contato" abre canal (formulário/e-mail) com a equipe de suporte, registrando o pedido com o e-mail do usuário.

---

### 2.9 Módulo de Sessão

#### RF-033 — Sair (Logout)
**Prioridade:** Alta
**Ator:** Usuário
**Descrição:** O usuário poderá deslogar da sua conta pela sidebar (**Sair**).

**Critérios de Aceitação:**
- [CA-033.1] Ao clicar em "Sair", a sessão é encerrada e o usuário é redirecionado para a tela de login/cadastro.
- [CA-033.2] Tentativas de acessar a área logada sem sessão ativa redirecionam para o login.
- [CA-033.3] Preferências (tema, idioma, região) permanecem salvas no perfil para a próxima sessão.

---

## 3. Requisitos Não-Funcionais

### 3.1 Performance

| ID | Requisito | Critério |
|----|-----------|----------|
| RNF-001 | Tempo de resposta da busca e dos filtros da Prateleira | ≤ 2 segundos para até 10.000 livros |
| RNF-002 | Disponibilidade do sistema | 99% durante o horário de operação (07h–22h) |
| RNF-003 | Job de verificação de atrasos | Executar diariamente às 00:00 |
| RNF-004 | Recálculo de avaliação média | ≤ 1 segundo após nova avaliação |

### 3.2 Segurança

| ID | Requisito | Critério |
|----|-----------|----------|
| RNF-005 | Senhas | Armazenadas com hash (bcrypt/argon2) |
| RNF-006 | Sessão | Expira após 30 minutos de inatividade |
| RNF-007 | E-mail | Validado quanto à unicidade e formato |
| RNF-008 | Cartão registrado | Dados sensíveis armazenados tokenizados/mascarados; exibição apenas parcial |

### 3.3 Usabilidade e Interface

| ID | Requisito | Critério |
|----|-----------|----------|
| RNF-009 | Interface | Responsiva (desktop e mobile), com sidebar adaptável |
| RNF-010 | Feedback | Mensagens claras para erros e sucessos |
| RNF-011 | Acessibilidade | Contraste adequado, fonte legível |
| RNF-012 | Identidade visual | Paleta `#0F194A` / `#8CC3F5` / `#FAF8F2`; Montserrat (títulos) e Open Sans (texto) |
| RNF-013 | Dark Mode | Alternância imediata de tema, persistida no perfil |
| RNF-014 | Internacionalização | Interface disponível em pt-BR e en |
| RNF-015 | Vitrine | Capas lado a lado; hover revela nome, preço do aluguel e avaliação; clique abre as 3 ações (Alugar, Entrar na fila, Pendente) |

---

## 4. Casos de Uso Detalhados

### UC-001 — Cadastro de Usuário

**Ator Principal:** Usuário
**Pré-condições:** Usuário não autenticado.

**Fluxo Principal:**
1. Usuário acessa a tela de cadastro.
2. Usuário informa nome, e-mail e senha.
3. Sistema valida campos obrigatórios, formato e unicidade do e-mail.
4. Sistema cria a conta com tipo "aluno".
5. Sistema autentica o usuário automaticamente.
6. Sistema redireciona o usuário para a aba **Início** da área logada (com sidebar).

**Fluxos Alternativos:**
- **3a. E-mail já cadastrado:** o sistema exibe "Este e-mail já está em uso".
- **3b. Senha menor que 6 caracteres:** o sistema solicita nova senha.

**Pós-condições:** Conta criada; sessão ativa; usuário na aba Início.

---

### UC-002 — Login

**Ator Principal:** Usuário
**Pré-condições:** Conta existente.

**Fluxo Principal:**
1. Usuário informa e-mail e senha.
2. Sistema valida credenciais.
3. Sistema inicia a sessão e redireciona para a aba **Início**.

**Fluxos Alternativos:**
- **2a. Credenciais inválidas:** mensagem genérica "E-mail ou senha incorretos".

---

### UC-003 — Alugar Livro Disponível

**Ator Principal:** Usuário
**Pré-condições:**
- Usuário autenticado e na área logada (Início, Prateleira ou Sugestões).
- Livro possui ao menos 1 de suas 3 cópias disponíveis.
- Não há fila de espera para o livro.

**Fluxo Principal:**
1. Usuário visualiza as capas lado a lado na vitrine.
2. Usuário passa o mouse sobre a capa: sistema exibe **nome, preço do aluguel e avaliação**.
3. Usuário clica na capa: sistema exibe os botões **"Alugar", "Entrar na fila" e "Pendente"** (somente "Alugar" habilitado).
4. Usuário clica em "Alugar".
5. Sistema valida elegibilidade (sem multa pendente, não bloqueado, < 3 livros alugados).
6. Sistema seleciona um exemplar disponível e cobra o preço do aluguel no cartão registrado.
7. Sistema cria registro de ALUGUEL com data_devolucao_prevista (15 ou 30 dias).
8. Sistema altera o status do exemplar para "alugado".
9. Sistema envia notificação de confirmação (e-mail + in-app).
10. Sistema exibe tela de confirmação com prazo de devolução.

**Fluxos Alternativos:**
- **3a. Cópias esgotadas com fila < 10:** "Entrar na fila" habilitado; o caso de uso continua em UC-004.
- **3b. Fila cheia (10):** "Entrar na fila" desabilitado e "Pendente" habilitado; o caso de uso continua em UC-005.
- **5a. Usuário inelegível:** mensagem específica ("Você possui multas pendentes", "Você está bloqueado" ou "Limite de 3 livros atingido").

**Pós-condições:** Exemplar "alugado"; aluguel "ativo"; cobrança registrada.

---

### UC-004 — Entrar na Fila de Espera

**Ator Principal:** Usuário
**Pré-condições:**
- Usuário autenticado.
- As 3 cópias estão alugadas OU já existe fila.
- Fila possui menos de 10 pessoas.

**Fluxo Principal:**
1. Usuário clica na capa do livro indisponível.
2. Sistema exibe "Entrar na fila" habilitado.
3. Usuário clica em "Entrar na fila".
4. Sistema valida elegibilidade do usuário.
5. Sistema cria registro em FILA_ESPERA com posição = última + 1.
6. Sistema define status como "aguardando".
7. Sistema exibe confirmação com posição na fila.

**Fluxos Alternativos:**
- **2a. Fila cheia (10):** o sistema exibe "Pendente" no lugar de "Entrar na fila" (ver UC-005).
- **4a. Usuário inelegível:** mensagem de erro e bloqueio da entrada.

**Pós-condições:** Usuário na fila; posição registrada.

---

### UC-005 — Marcar-se como Pendente

**Ator Principal:** Usuário
**Pré-condições:**
- Usuário autenticado.
- Fila do livro cheia (10 pessoas).

**Fluxo Principal:**
1. Usuário clica na capa do livro com fila cheia.
2. Sistema exibe "Pendente" habilitado (e "Entrar na fila" desabilitado).
3. Usuário clica em "Pendente".
4. Sistema registra o usuário na LISTA_PENDENTES (ordem de chegada).
5. Sistema exibe confirmação: "Você será incluído na fila quando abrir uma vaga".

**Fluxos Alternativos:**
- **4a. Já pendente para o livro:** o sistema informa que o registro já existe.

**Pós-condições:** Usuário na lista de pendentes; será promovido automaticamente à fila (FIFO) quando uma vaga se abrir (desistência, expiração ou confirmação de aluguel por alguém da fila).

---

### UC-006 — Marcar Livro como "Lido" ou "Lista de Desejos"

**Ator Principal:** Usuário
**Pré-condições:** Usuário autenticado; livro exibido na vitrine.

**Fluxo Principal:**
1. Usuário clica na capa do livro (ou no menu de ações).
2. Sistema exibe as opções "Marcar como Lido" e "Adicionar à Lista de Desejos".
3. Usuário escolhe a marcação desejada.
4. Sistema registra em JA_LIDOS ou LISTA_DESEJOS.
5. Sistema atualiza as abas correspondentes da sidebar.

**Pós-condições:** Livro presente na coleção escolhida; se "Lido", o livro fica elegível para avaliação.

---

### UC-007 — Avaliar Livro

**Ator Principal:** Usuário
**Pré-condições:**
- Usuário autenticado.
- Livro marcado como "Lido" pelo usuário (para emitir avaliação).

**Fluxo Principal:**
1. Usuário acessa a aba "Avaliar livros".
2. Sistema lista os livros lidos ainda não avaliados e as avaliações já feitas.
3. Usuário seleciona um livro lido e atribui de 0 a 5 estrelas.
4. Sistema registra a avaliação.
5. Sistema recalcula a avaliação média do livro (exibida no hover da capa).
6. Sistema confirma o registro.

**Fluxos Alternativos:**
- **3a. Livro não lido:** o sistema informa que apenas livros marcados como "Lido" podem ser avaliados (mas a média do livro pode ser visualizada).

---

### UC-013 — Registrar Devolução

**Ator Principal:** Bibliotecário (ou Usuário via sistema)
**Pré-condições:**
- O exemplar está com status "alugado".
- Existe um registro de ALUGUEL ativo para o exemplar.

**Fluxo Principal:**
1. Bibliotecário acessa a tela de devolução ou usuário inicia devolução pelo sistema.
2. Sistema identifica o aluguel ativo vinculado ao exemplar.
3. Sistema verifica se há atraso (data_real > data_prevista).
4. Se houver atraso, sistema calcula multa (R$ 3,00/dia, teto = valor do livro).
5. Sistema atualiza data_devolucao_real e status do aluguel para "devolvido".
6. Sistema verifica se existe fila de espera para o livro.
7. Se não houver fila, exemplar muda para "disponível".
8. Se houver fila, sistema notifica o primeiro usuário (e-mail + in-app).
9. Sistema define status do primeiro da fila como "notificado" e data_limite = hoje + 3.
10. Sistema exibe confirmação de devolução.

**Fluxos Alternativos:**
- **3a. Livro não está emprestado:** "Este livro não está emprestado."
- **8a. Múltiplas cópias devolvidas:** cada cópia notifica o próximo da fila individualmente, uma de cada vez.
- **8b. Desistência/expiração na fila:** o primeiro pendente é promovido à fila e notificado.

**Pós-condições:** Aluguel "devolvido"; exemplar "disponível" ou "reservado" (para fila); multa gerada se houver atraso.

---

### UC-014 — Aplicar Multa por Dano

**Ator Principal:** Bibliotecário
**Pré-condições:**
- O exemplar foi devolvido.
- O bibliotecário identificou dano físico no exemplar.

**Fluxo Principal:**
1. Bibliotecário acessa o registro de devolução.
2. Bibliotecário seleciona a opção "Aplicar multa por dano".
3. Sistema exibe dropdown com tipos de dano (capa rasgada, páginas faltantes, etc.).
4. Bibliotecário seleciona o tipo de dano.
5. Sistema exibe valor fixo de R$ 20,00 (readonly).
6. Bibliotecário confirma a aplicação.
7. Sistema cria registro de MULTA com tipo "dano".
8. Sistema altera status do exemplar para "danificado".
9. Sistema notifica o usuário (e-mail + in-app).

**Pós-condições:** Multa de dano registrada; exemplar marcado como "danificado".

---

## 5. Regras de Negócio

### RN-001 — Tamanho do Livro e Prazo de Aluguel
- **Tamanho (4 faixas automáticas):**
  - Pequeno: até 100 páginas.
  - Médio-pequeno: mais de 100 até 150 páginas.
  - Médio-padrão: mais de 150 até 250 páginas.
  - Grande: mais de 250 páginas.
- **Prazo de aluguel:**
  - Livros de até 150 páginas (Pequeno e Médio-pequeno): **15 dias**.
  - Livros com mais de 150 páginas (Médio-padrão e Grande): **30 dias**.

### RN-002 — Extensão de Prazo
- Máximo de 2 extensões por aluguel.
- 1ª extensão: +2 meses.
- 2ª extensão: +1 mês.
- Extensão só permitida se não houver fila de espera.
- Se alguém entrar na fila durante a extensão: devolução obrigatória em 3 dias.

### RN-003 — Fila de Espera e Lista de Pendentes
- Cada título possui **3 cópias**; o aluguel exige ao menos 1 cópia livre **e** fila vazia.
- Se não houver ninguém na fila (há cópias livres), o usuário **não pode** entrar na fila — apenas alugar.
- Limite máximo da fila: **10 pessoas** por livro.
- Quando a fila estiver cheia, o usuário pode marcar **"Pendente"**; ao abrir vaga na fila, o primeiro pendente é promovido automaticamente (FIFO).
- Ordem da fila: por chegada (FIFO).
- Cópias disponíveis vão para o primeiro da fila, mesmo que haja cópias livres.
- Se há fila, novos usuários só veem "Entrar na fila" (ou "Pendente", se a fila estiver cheia).

### RN-004 — Multa por Atraso
- R$ 3,00 por dia de atraso.
- Teto: valor do livro.
- Após o teto: +1 dia de bloqueio por dia adicional de atraso.
- Atraso > 15 dias: bloqueio de 1 semana (a partir do dia 16).

### RN-005 — Multa por Dano
- Valor fixo: R$ 20,00.
- Aplicada manualmente pelo bibliotecário.
- Cumulativa com multa de atraso.

### RN-006 — Bloqueio de Usuário
- Usuário com multa pendente ou atraso ativo: impedido de alugar, entrar em fila ou ficar pendente.
- Usuário com 3 livros alugados: impedido de alugar e entrar em fila.
- Usuário bloqueado por penalidade: impedido até a data de desbloqueio.

### RN-007 — Notificações
- Canal duplo: e-mail + notificação in-app.
- Prazo de resposta na fila: 3 dias.
- Notificação de prazo próximo: 3 dias antes do vencimento.
- Notificação obrigatória na promoção de pendente → fila.

### RN-008 — Preço do Aluguel
- Cada livro possui um preço de aluguel definido no cadastro (RF-004).
- O preço é exibido no hover da capa e cobrado no ato do aluguel (ou da confirmação pela fila).
- Multas usam o **valor do livro** como teto (não o preço do aluguel).

### RN-009 — Avaliações
- Avaliação (0–5 estrelas) somente para livros marcados como "Lido".
- Um usuário avalia cada livro uma única vez, podendo editar depois.
- A média exibida (hover, Destaques, fichas) é recalculada a cada avaliação.

### RN-010 — Sugestões
- O sistema contabiliza o uso dos filtros de tamanho, gênero e autor por usuário.
- As sugestões priorizam gênero > autor > tamanho, pelos mais usados.
- Livros ativamente alugados ou já lidos têm prioridade reduzida.

### RN-011 — Novidades e Destaques
- **Novidades:** `data_lancamento` nos últimos 6 meses.
- **Destaques:** aluguéis no último ano acima do percentil 80 do acervo **e** avaliação média ≥ 4,5.

---

## 6. Modelo de Dados

### 6.1 Escolhas Tecnológicas (Preparado para MySQL)

| Decisão | Valor |
|---------|-------|
| SGBD | **MySQL 8.0+** |
| Engine | **InnoDB** (transações e FKs) |
| Charset/Collation | **utf8mb4** / utf8mb4_0900_ai_ci (acentos e emojis) |
| Chaves primárias | `BIGINT UNSIGNED AUTO_INCREMENT` |
| Moeda | `DECIMAL(10,2)` (nunca FLOAT) |
| Datas | `DATE` (prazos) e `DATETIME` (eventos) |
| Exclusão de títulos/usuários | Soft delete via `deleted_at` (RF-007, CA-007.3) |
| Auditoria | `created_at DATETIME DEFAULT CURRENT_TIMESTAMP` em todas as tabelas |

### 6.2 Enums de Status (consolidados — máquinas de estado)

```
EXEMPLAR.status:        disponivel | alugado | danificado | manutencao
ALUGUEL.status:         ativo | devolvido
FILA_ESPERA.status:     aguardando | notificado | confirmado | expirado | desistiu
LISTA_PENDENTES.status: ativo | promovido | cancelado
MULTA.status:           pendente | pago
MULTA.tipo:             atraso | dano
USUARIO.tipo:           aluno | bibliotecario | administrador
USUARIO.idioma:         pt-BR | en
USUARIO.tema:           claro | escuro
LIVRO.faixa_tamanho:    pequeno | medio_pequeno | medio_padrao | grande
USO_FILTRO.tipo_filtro: tamanho | genero | autor
```

**Transições-chave (fonte: RFs do módulo 2.3):**
- `EXEMPLAR`: disponivel → alugado (RF-010) → disponivel (devolução sem fila, RF-012) ou → danificado (multa por dano, RF-017).
- `FILA_ESPERA`: aguardando → notificado (devolução, RF-012) → confirmado (RF-013) | expirado (RF-013.5) | desistiu (RF-015); posições 1–10 (RN-003).
- `LISTA_PENDENTES`: ativo → promovido (vaga na fila, RF-011A) | cancelado (RF-015).

### 6.3 Definição Física das Tabelas (MySQL 8.0)

```sql
USUARIO (
  id_usuario        BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nome              VARCHAR(150)  NOT NULL,
  email             VARCHAR(190)  NOT NULL UNIQUE,
  senha_hash        VARCHAR(255)  NOT NULL,             -- bcrypt/argon2 (RNF-005)
  tipo              ENUM('aluno','bibliotecario','administrador') NOT NULL DEFAULT 'aluno',
  foto_url          VARCHAR(500)  NULL,
  idioma            ENUM('pt-BR','en') NOT NULL DEFAULT 'pt-BR',
  tema              ENUM('claro','escuro') NOT NULL DEFAULT 'claro',
  uf                CHAR(2)       NULL,                  -- região (RF-031)
  cidade            VARCHAR(120)  NULL,                  -- região (RF-031)
  end_logradouro    VARCHAR(190)  NULL,
  end_numero        VARCHAR(20)   NULL,
  end_complemento   VARCHAR(120)  NULL,
  end_bairro        VARCHAR(120)  NULL,
  end_cidade        VARCHAR(120)  NULL,
  end_uf            CHAR(2)       NULL,
  end_cep           CHAR(8)       NULL,                  -- somente dígitos; validado na aplicação (RF-028)
  cartao_token      VARCHAR(255)  NULL,                  -- tokenizado (RNF-008)
  cartao_mascarado  VARCHAR(25)   NULL,                  -- ex.: "**** 1234"
  bloqueado_ate     DATE          NULL,
  deleted_at        DATETIME      NULL,
  created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

LIVRO (
  id_livro             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  titulo               VARCHAR(255) NOT NULL,
  autor                VARCHAR(190) NOT NULL,
  genero               VARCHAR(80)  NOT NULL,
  faixa_tamanho        ENUM('pequeno','medio_pequeno','medio_padrao','grande') NOT NULL, -- RN-001
  num_paginas          INT UNSIGNED NOT NULL CHECK (num_paginas > 0),
  valor_livro          DECIMAL(10,2) NOT NULL CHECK (valor_livro >= 0),    -- teto da multa (RN-004)
  preco_aluguel        DECIMAL(10,2) NOT NULL CHECK (preco_aluguel >= 0),  -- RN-008
  capa_url             VARCHAR(500) NULL,
  data_lancamento      DATE         NULL,              -- alimenta Novidades (RN-011)
  avaliacao_media      DECIMAL(3,2) NOT NULL DEFAULT 0 CHECK (avaliacao_media BETWEEN 0 AND 5), -- denormalizado, RN-009
  total_avaliacoes     INT UNSIGNED NOT NULL DEFAULT 0,
  alugueis_ultimo_ano  INT UNSIGNED NOT NULL DEFAULT 0, -- alimenta Destaques (RN-011)
  eh_infantil          TINYINT(1)   NOT NULL DEFAULT 0,
  deleted_at           DATETIME     NULL,
  created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

EXEMPLAR (
  id_exemplar     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_livro        BIGINT UNSIGNED NOT NULL,
  codigo_barras   VARCHAR(64) NULL UNIQUE,             -- CA-005.2
  status          ENUM('disponivel','alugado','danificado','manutencao')
                  NOT NULL DEFAULT 'disponivel',
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_livro) REFERENCES LIVRO(id_livro)
    ON DELETE RESTRICT                                  -- RF-007.1/7.2
);

ALUGUEL (
  id_aluguel               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_usuario               BIGINT UNSIGNED NOT NULL,
  id_exemplar              BIGINT UNSIGNED NOT NULL,
  data_aluguel             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  data_devolucao_prevista  DATE NOT NULL,               -- hoje+15 ou hoje+30 (RN-001)
  data_devolucao_real      DATE NULL,
  status                   ENUM('ativo','devolvido') NOT NULL DEFAULT 'ativo',
  extensao_contador        TINYINT UNSIGNED NOT NULL DEFAULT 0 CHECK (extensao_contador <= 2),
  preco_cobrado            DECIMAL(10,2) NOT NULL,      -- preco_aluguel no momento do aluguel
  created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_usuario)  REFERENCES USUARIO(id_usuario)  ON DELETE RESTRICT,
  FOREIGN KEY (id_exemplar) REFERENCES EXEMPLAR(id_exemplar) ON DELETE RESTRICT
);

FILA_ESPERA (
  id_fila                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_livro               BIGINT UNSIGNED NOT NULL,
  id_usuario             BIGINT UNSIGNED NOT NULL,
  posicao                TINYINT UNSIGNED NOT NULL CHECK (posicao BETWEEN 1 AND 10), -- RN-003
  status                 ENUM('aguardando','notificado','confirmado','expirado','desistiu')
                         NOT NULL DEFAULT 'aguardando',
  data_entrada           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  data_notificacao       DATETIME NULL,
  data_limite_resposta   DATE NULL,                     -- hoje+3 (RF-012.6)
  created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_livro)   REFERENCES LIVRO(id_livro)     ON DELETE RESTRICT,
  FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario) ON DELETE RESTRICT
);

LISTA_PENDENTES (
  id_pendente     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_livro        BIGINT UNSIGNED NOT NULL,
  id_usuario      BIGINT UNSIGNED NOT NULL,
  status          ENUM('ativo','promovido','cancelado') NOT NULL DEFAULT 'ativo',
  data_entrada    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  data_promocao   DATETIME NULL,
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_livro)   REFERENCES LIVRO(id_livro)     ON DELETE RESTRICT,
  FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario) ON DELETE RESTRICT
);

JA_LIDOS (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_usuario    BIGINT UNSIGNED NOT NULL,
  id_livro      BIGINT UNSIGNED NOT NULL,
  data_marcacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_lido (id_usuario, id_livro),
  FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario) ON DELETE CASCADE,
  FOREIGN KEY (id_livro)   REFERENCES LIVRO(id_livro)     ON DELETE CASCADE
);

LISTA_DESEJOS (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_usuario    BIGINT UNSIGNED NOT NULL,
  id_livro      BIGINT UNSIGNED NOT NULL,
  data_marcacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_desejo (id_usuario, id_livro),
  FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario) ON DELETE CASCADE,
  FOREIGN KEY (id_livro)   REFERENCES LIVRO(id_livro)     ON DELETE CASCADE
);

AVALIACAO (
  id_avaliacao    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_usuario      BIGINT UNSIGNED NOT NULL,
  id_livro        BIGINT UNSIGNED NOT NULL,
  nota            DECIMAL(2,1) NOT NULL CHECK (nota BETWEEN 0 AND 5), -- RN-009
  data_avaliacao  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_avaliacao (id_usuario, id_livro),
  FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario) ON DELETE CASCADE,
  FOREIGN KEY (id_livro)   REFERENCES LIVRO(id_livro)     ON DELETE CASCADE
);

USO_FILTRO (
  id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_usuario   BIGINT UNSIGNED NOT NULL,
  tipo_filtro  ENUM('tamanho','genero','autor') NOT NULL,
  valor        VARCHAR(120) NOT NULL,
  contagem     INT UNSIGNED NOT NULL DEFAULT 0,
  UNIQUE KEY uq_uso (id_usuario, tipo_filtro, valor),
  FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario) ON DELETE CASCADE
);

MULTA (
  id_multa        BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_aluguel      BIGINT UNSIGNED NOT NULL,
  id_usuario      BIGINT UNSIGNED NOT NULL,
  tipo            ENUM('atraso','dano') NOT NULL,
  valor           DECIMAL(10,2) NOT NULL CHECK (valor > 0),
  dias_atraso     SMALLINT UNSIGNED NULL,
  status          ENUM('pendente','pago') NOT NULL DEFAULT 'pendente',
  data_pagamento  DATETIME NULL,
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_aluguel) REFERENCES ALUGUEL(id_aluguel) ON DELETE RESTRICT,
  FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario) ON DELETE RESTRICT
);
```

### 6.4 Índices e Justificativas

| Índice | Justificativa |
|--------|---------------|
| `LIVRO(titulo)`, `LIVRO(autor)`, `LIVRO(genero)` | Busca parcial da vitrine (RF-009, CA-009.4); para escala, adicionar índice **FULLTEXT** em `(titulo, autor)` |
| `LIVRO(faixa_tamanho)`, `LIVRO(data_lancamento)`, `LIVRO(eh_infantil)`, `LIVRO(avaliacao_media)` | Filtros da Prateleira: tamanho, novidades, infantis e destaques (RF-022, RN-011) |
| `LIVRO(alugueis_ultimo_ano)` | Filtro Destaques (RN-011) |
| `EXEMPLAR(id_livro, status)` | Verificar disponibilidade das 3 cópias em tempo de aluguel (RF-010) |
| `ALUGUEL(data_devolucao_prevista)` | Job diário de atrasos (RF-016.1, RNF-003) |
| `ALUGUEL(id_usuario, status)` | Regras de elegibilidade: "< 3 livros alugados", multas pendentes (RN-006) |
| `FILA_ESPERA(id_livro, posicao)` | Próximo da fila e controle de limite de 10 (RN-003) |
| `LISTA_PENDENTES(id_livro, status, data_entrada)` | Promoção FIFO de pendentes (RF-011A) |
| `MULTA(id_usuario, status)` | Bloqueio por multa pendente (RN-006) e tela de multas (RF-018) |
| `USUARIO(email)` UNIQUE | RF-001/RF-002 |

### 6.5 Integridade e Regras Transacionais

- [INT-1] Todas as operações de aluguel, fila, pendente e devolução devem ocorrer em **transação** com `SELECT ... FOR UPDATE` sobre o `LIVRO`/`EXEMPLAR` para evitar condições de corrida nas 3 cópias e nas posições da fila.
- [INT-2] **Máximo de 3 exemplares por livro** (CA-005.4): verificado na aplicação dentro da transação de cadastro de exemplar.
- [INT-3] **Fila limitada a 10** (CA-011.5) e **posições 1–10** (CHECK): inserção condicionada à contagem atual na transação.
- [INT-4] **Um registro ativo por usuário/livro** em `FILA_ESPERA` e `LISTA_PENDENTES` (CA-011A.2): MySQL não tem índice parcial; usar coluna gerada (ex.: `usuario_livro_ativo` gerada quando `status='aguardando'`) com UNIQUE, ou trava de aplicação.
- [INT-5] **Avaliação exige "Lido"** (CA-026.1): `EXISTS` em `JA_LIDOS` validado na aplicação/transação de escrita.
- [INT-6] FKs com `ON DELETE RESTRICT` para `LIVRO`, `USUARIO`, `ALUGUEL` (histórico preservado); `CASCADE` apenas nas coleções pessoais (`JA_LIDOS`, `LISTA_DESEJOS`, `AVALIACAO`, `USO_FILTRO`).
- [INT-7] Soft delete: excluir título = preencher `deleted_at` (RF-007.3); todas as consultas do catálogo filtram `deleted_at IS NULL`.

### 6.6 Denormalização e Recálculo

| Campo | Estratégia |
|-------|-----------|
| `LIVRO.avaliacao_media`, `total_avaliacoes` | Recalculados a cada insert/update em `AVALIACAO` (RN-009); deve atender RNF-004 (≤1s). Implementar na aplicação ou em trigger. |
| `LIVRO.alugueis_ultimo_ano` | Incrementado no ato do aluguel; **job mensal** decrementa/remove registros com mais de 1 ano (mantém coerência com RN-011). |
| Destaques | Calculado por consulta: `avaliacao_media >= 4.5 AND alugueis_ultimo_ano >= percentil_80` (RN-011); sem novo campo. |

### 6.7 Migrations e Seeds

- Migrations versionadas (uma por tabela/alteração), reversíveis.
- Seeds obrigatórios: 1 administrador, 1 bibliotecário, 1 aluno de teste; catálogo de exemplo cobrindo todas as faixas de tamanho, gêneros principais e datas de lançamento recentes (alimenta Novidades e Destaques — ver T-014).

---

## 7. Glossário

| Termo | Significado |
|-------|-------------|
| Aluguel | Registro de empréstimo de um exemplar para um usuário |
| Exemplar | Cópia física de um livro no acervo (máx. 3 por título) |
| Fila | Lista ordenada de espera por um livro (máx. 10 pessoas) |
| Pendente | Interesse registrado quando a fila está cheia; promovido à fila quando abre vaga |
| Extensão | Prorrogação do prazo de devolução |
| Multa | Penalidade financeira por atraso ou dano |
| Bloqueio | Suspensão temporária dos privilégios de aluguel |
| Notificação | Comunicação enviada ao usuário (e-mail + in-app) |
| Já Lidos | Coleção de livros marcados como lidos |
| Lista de Desejos | Coleção de livros desejados pelo usuário |
| Novidades | Livros lançados nos últimos 6 meses |
| Destaques | Livros muito alugados no último ano e bem avaliados |
| Dark Mode | Tema escuro da interface |
| Sidebar | Menu lateral da área logada |

---

## 8. Histórico de Revisões

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-09-03 | Equipe Spec-Driven | Especificação inicial aprovada |
| 2.1 | 2026-09-24 | Equipe Spec-Driven | Fechamento do design físico de banco de dados para **MySQL 8.0**: tipos, enums de status consolidados, constraints, índices com justificativas, regras transacionais (§6.5), estratégia de denormalização (§6.6) e migrations/seeds (§6.7) |
| 2.0 | 2026-09-22 | Equipe Spec-Driven | Integração do briefing de interface do **Aurora**: identidade visual (paleta e fontes), área logada com sidebar, Prateleira com 6 filtros (tamanho em 4 faixas, gênero, autor, novidades, destaques, infantis), Sugestões por filtros mais usados, coleções Já lidos e Lista de desejos, avaliações 0–5 estrelas, configurações (informações do usuário com cartão/foto/endereço+CEP, dark mode, idioma pt-BR/en, região estado/cidade, ajuda e suporte), logout; regras de aluguel revisadas (3 cópias por título, fila máx. 10, novo status "Pendente" com promoção automática, preço do aluguel exibido no hover, login por e-mail); modelo de dados e RN atualizados |

---

*Fim do documento*
