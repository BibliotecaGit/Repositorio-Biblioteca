# Especificação Técnica — Sistema de Biblioteca com Aluguel de Livros

**Versão:** 1.0  
**Data:** 2026-09-03  
**Status:** Aprovado para desenvolvimento  
**Autor:** Equipe de Análise Spec-Driven  

---

## 1. Introdução

### 1.1 Propósito

Esta especificação define os requisitos funcionais, regras de negócio, casos de uso e critérios de aceitação para o desenvolvimento de um sistema de gerenciamento de biblioteca escolar com controle de aluguel de livros, fila de espera, multas por atraso e extensão de prazos.

### 1.2 Escopo

O sistema será utilizado por alunos, bibliotecários e administradores de uma instituição escolar para gerenciar o acervo de livros, controlar empréstimos, gerenciar filas de espera e aplicar penalidades por atraso ou danos.

### 1.3 Definições e Acrônimos

| Termo | Definição |
|-------|-----------|
| Aluguel | Empréstimo de um exemplar físico de livro para um usuário cadastrado |
| Exemplar | Unidade física de um livro (uma cópia) |
| Fila de Espera | Lista ordenada de usuários aguardando a disponibilidade de um livro |
| Extensão | Prolongamento do prazo de devolução mediante solicitação do usuário |
| Multa | Valor monetário aplicado por atraso ou dano ao exemplar |
| Bloqueio | Período em que o usuário fica impedido de realizar novos aluguéis |

### 1.4 Referências

- Análise Spec-Driven — Rodadas 1, 2 e 3
- Documento de Regras de Negócio Consolidadas
- Modelagem Técnica (Casos de Uso, MER, Máquina de Estados)

---

## 2. Requisitos Funcionais

### 2.1 Módulo de Autenticação e Cadastro

#### RF-001 — Cadastro de Usuário
**Prioridade:** Alta  
**Ator:** Aluno  
**Descrição:** O sistema deve permitir que novos usuários se cadastrem informando nome, e-mail, turma, CPF e senha. O CPF deve ser único no sistema.

**Critérios de Aceitação:**
- [CA-001.1] Todos os campos (nome, e-mail, turma, CPF, senha) são obrigatórios.
- [CA-001.2] O CPF deve ser validado quanto à unicidade. CPFs duplicados devem ser rejeitados com mensagem de erro.
- [CA-001.3] O e-mail deve ser validado quanto à unicidade.
- [CA-001.4] A senha deve ter no mínimo 6 caracteres.
- [CA-001.5] O campo "turma" é informativo e não gera restrições automáticas.
- [CA-001.6] O tipo de usuário padrão no cadastro é "aluno".

#### RF-002 — Login
**Prioridade:** Alta  
**Ator:** Aluno, Bibliotecário, Administrador  
**Descrição:** O sistema deve permitir que usuários cadastrados realizem login utilizando CPF e senha.

**Critérios de Aceitação:**
- [CA-002.1] O login é obrigatório para todas as operações de aluguel, fila e extensão.
- [CA-002.2] Credenciais inválidas devem retornar mensagem genérica de erro (não revelar qual campo está incorreto).
- [CA-002.3] Sessão deve expirar após 30 minutos de inatividade.

#### RF-003 — Controle de Acesso por Perfil
**Prioridade:** Alta  
**Ator:** Sistema  
**Descrição:** O sistema deve diferenciar permissões com base no tipo de usuário (aluno, bibliotecário, administrador).

**Critérios de Aceitação:**
- [CA-003.1] Alunos têm acesso apenas a operações de aluguel, fila e visualização de histórico.
- [CA-003.2] Bibliotecários podem gerenciar o acervo (livros e exemplares) e aplicar multas por dano.
- [CA-003.3] Administradores podem categorizar títulos (gênero, tamanho, autor) e excluir títulos do sistema.

---

### 2.2 Módulo de Acervo

#### RF-004 — Cadastro de Livro
**Prioridade:** Alta  
**Ator:** Bibliotecário  
**Descrição:** O sistema deve permitir o cadastro de novos títulos no acervo com título, autor, gênero, número de páginas e valor do livro.

**Critérios de Aceitação:**
- [CA-004.1] Todos os campos são obrigatórios.
- [CA-004.2] O campo "tamanho" é determinado automaticamente: ≤150 páginas = "pequeno", >150 = "grande".
- [CA-004.3] O campo "gênero" é preenchido pelo bibliotecário, mas pode ser reorganizado pelo administrador.
- [CA-004.4] O valor do livro é utilizado como teto para multas de atraso.

#### RF-005 — Cadastro de Exemplar
**Prioridade:** Alta  
**Ator:** Bibliotecário  
**Descrição:** O sistema deve permitir o cadastro de exemplares físicos vinculados a um livro, com código de barras opcional.

**Critérios de Aceitação:**
- [CA-005.1] Cada exemplar deve estar vinculado a um livro existente.
- [CA-005.2] O código de barras, quando informado, deve ser único.
- [CA-005.3] O status inicial do exemplar é "disponível".
- [CA-005.4] Um livro pode ter N exemplares.

#### RF-006 — Edição de Livro
**Prioridade:** Média  
**Ator:** Bibliotecário  
**Descrição:** O sistema deve permitir a edição das informações de um livro já cadastrado.

**Critérios de Aceitação:**
- [CA-006.1] Alteração no número de páginas deve recalcular o tamanho (pequeno/grande).
- [CA-006.2] Alteração no valor do livro deve refletir no teto de multas futuras (multas já geradas não são alteradas).

#### RF-007 — Exclusão de Título
**Prioridade:** Média  
**Ator:** Administrador  
**Descrição:** O sistema deve permitir que o administrador exclua títulos do acervo.

**Critérios de Aceitação:**
- [CA-007.1] Não é permitido excluir um livro que possua exemplares atualmente alugados.
- [CA-007.2] Não é permitido excluir um livro que possua usuários na fila de espera.
- [CA-007.3] A exclusão deve ser lógica (soft delete) ou com confirmação explícita.

#### RF-008 — Categorização de Títulos
**Prioridade:** Média  
**Ator:** Administrador  
**Descrição:** O sistema deve permitir que o administrador organize os títulos por gênero, tamanho e autor.

**Critérios de Aceitação:**
- [CA-008.1] O administrador pode editar o gênero de qualquer livro.
- [CA-008.2] O administrador pode alterar a classificação de tamanho manualmente, sobrescrevendo a regra automática.
- [CA-008.3] Mudanças de categoria não afetam aluguéis em andamento.

---

### 2.3 Módulo de Aluguel

#### RF-009 — Busca de Livros
**Prioridade:** Alta  
**Ator:** Aluno  
**Descrição:** O sistema deve permitir a busca de livros por título, autor ou gênero.

**Critérios de Aceitação:**
- [CA-009.1] A busca deve retornar resultados parciais (LIKE/contains).
- [CA-009.2] Cada resultado deve exibir: título, autor, gênero, número de páginas, quantidade de cópias disponíveis e quantidade de pessoas na fila.
- [CA-009.3] O botão exibido depende do estado do livro (ver RF-010 e RF-011).

#### RF-010 — Aluguel de Livro Disponível
**Prioridade:** Alta  
**Ator:** Aluno  
**Descrição:** O sistema deve permitir que um aluno alugue um livro que possua exemplares disponíveis e não tenha fila de espera.

**Critérios de Aceitação:**
- [CA-010.1] O aluno deve estar autenticado.
- [CA-010.2] O aluno não pode ter atrasos ativos.
- [CA-010.3] O aluno não pode estar bloqueado.
- [CA-010.4] O aluno não pode ter 3 ou mais livros alugados simultaneamente.
- [CA-010.5] Se há fila de espera para o livro, o botão "Reservar" não deve ser exibido; deve-se exibir "Entrar na fila".
- [CA-010.6] Se não há fila e há cópias disponíveis, o botão "Reservar" é exibido.
- [CA-010.7] Ao reservar, o sistema seleciona automaticamente um exemplar disponível.
- [CA-010.8] A data de devolução prevista é calculada: hoje + 15 dias (pequeno) ou + 30 dias (grande).
- [CA-010.9] O status do exemplar muda para "alugado".
- [CA-010.10] Uma notificação de confirmação é enviada por e-mail e pelo sistema.

#### RF-011 — Entrada na Fila de Espera
**Prioridade:** Alta  
**Ator:** Aluno  
**Descrição:** O sistema deve permitir que um aluno entre na fila de espera de um livro quando todas as cópias estão alugadas ou quando já existe uma fila.

**Critérios de Aceitação:**
- [CA-011.1] O aluno deve estar autenticado.
- [CA-011.2] O aluno não pode ter atrasos ativos.
- [CA-011.3] O aluno não pode estar bloqueado.
- [CA-011.4] O aluno não pode ter 3 ou mais livros alugados.
- [CA-011.5] A fila não pode ter 5 ou mais pessoas.
- [CA-011.6] Se a fila está cheia (5 pessoas), o botão deve ser desabilitado com o texto "Fila cheia".
- [CA-011.7] A posição na fila é determinada por ordem de chegada (última posição + 1).
- [CA-011.8] O status na fila é "aguardando".
- [CA-011.9] O aluno pode desistir da fila a qualquer momento.

#### RF-012 — Devolução de Livro
**Prioridade:** Alta  
**Ator:** Aluno, Bibliotecário  
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
**Ator:** Aluno  
**Descrição:** O sistema deve permitir que o usuário notificado na fila confirme o aluguel do livro.

**Critérios de Aceitação:**
- [CA-013.1] O usuário deve clicar no botão "Confirmar aluguel" dentro do prazo de 3 dias.
- [CA-013.2] Ao confirmar, o sistema cria um novo registro de ALUGUEL.
- [CA-013.3] O exemplar muda para "alugado".
- [CA-013.4] O status na fila muda para "confirmado".
- [CA-013.5] Se o usuário não confirmar em 3 dias, o status muda para "expirado" e o próximo da fila é notificado.
- [CA-013.6] Se o usuário desistir, o status muda para "desistiu" e o próximo da fila é notificado imediatamente.

#### RF-014 — Solicitação de Extensão de Prazo
**Prioridade:** Alta  
**Ator:** Aluno  
**Descrição:** O sistema deve permitir que o aluno solicite a extensão do prazo de devolução.

**Critérios de Aceitação:**
- [CA-014.1] A extensão só é permitida se não houver ninguém na fila de espera para o livro.
- [CA-014.2] O contador de extensões deve ser menor que 2.
- [CA-014.3] 1ª extensão: data_devolucao = hoje + 2 meses; contador = 1.
- [CA-014.4] 2ª extensão: data_devolucao = hoje + 1 mês; contador = 2.
- [CA-014.5] A extensão requer ação explícita do usuário (não é automática).
- [CA-014.6] Se alguém entrar na fila durante o período de extensão, o usuário é notificado e tem 3 dias para devolver obrigatoriamente.
- [CA-014.7] Se o usuário não devolver em 3 dias após a notificação de entrada na fila, inicia-se multa de atraso normalmente.
- [CA-014.8] Após a 2ª extensão, a devolução é obrigatória e notificada no sistema.

#### RF-015 — Desistência da Fila
**Prioridade:** Média  
**Ator:** Aluno  
**Descrição:** O sistema deve permitir que um usuário desista da fila de espera.

**Critérios de Aceitação:**
- [CA-015.1] O usuário pode desistir a qualquer momento enquanto estiver na fila.
- [CA-015.2] Ao desistir, o status muda para "desistiu".
- [CA-015.3] O próximo usuário na fila é notificado imediatamente.
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
- [CA-016.7] O usuário fica impedido de alugar ou entrar em fila enquanto houver multa pendente ou bloqueio ativo.

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
**Ator:** Aluno  
**Descrição:** O sistema deve permitir que o aluno visualize suas multas pendentes e histórico.

**Critérios de Aceitação:**
- [CA-018.1] O aluno deve visualizar: tipo de multa, valor, dias de atraso (se aplicável), status (pendente/pago) e data de geração.
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
- [CA-019.6] Todas as notificações devem ser enviadas por e-mail e pelo sistema simultaneamente.

---

## 3. Requisitos Não-Funcionais

### 3.1 Performance

| ID | Requisito | Critério |
|----|-----------|----------|
| RNF-001 | Tempo de resposta da busca | ≤ 2 segundos para até 10.000 livros |
| RNF-002 | Disponibilidade do sistema | 99% durante o horário escolar (07h–22h) |
| RNF-003 | Job de verificação de atrasos | Executar diariamente às 00:00 |

### 3.2 Segurança

| ID | Requisito | Critério |
|----|-----------|----------|
| RNF-004 | Senhas | Armazenadas com hash (bcrypt/argon2) |
| RNF-005 | Sessão | Expira após 30 minutos de inatividade |
| RNF-006 | CPF | Validado quanto à unicidade e formato |

### 3.3 Usabilidade

| ID | Requisito | Critério |
|----|-----------|----------|
| RNF-007 | Interface | Responsiva (desktop e mobile) |
| RNF-008 | Feedback | Mensagens claras para erros e sucessos |
| RNF-009 | Acessibilidade | Contraste adequado, fonte legível |

---

## 4. Casos de Uso Detalhados

### UC-004 — Alugar Livro Disponível

**Ator Principal:** Aluno  
**Pré-condições:**
- Aluno está autenticado.
- Livro possui pelo menos um exemplar disponível.
- Não há fila de espera para o livro.

**Fluxo Principal:**
1. Aluno acessa a tela de busca de livros.
2. Aluno digita o título/autor/gênero desejado.
3. Sistema exibe lista de resultados com status de disponibilidade.
4. Aluno seleciona um livro com cópias disponíveis e sem fila.
5. Sistema exibe o botão "Reservar".
6. Aluno clica em "Reservar".
7. Sistema valida elegibilidade do aluno (sem atraso, não bloqueado, < 3 livros).
8. Sistema seleciona um exemplar disponível.
9. Sistema cria registro de ALUGUEL com data_devolucao_prevista.
10. Sistema altera status do exemplar para "alugado".
11. Sistema envia notificação de confirmação (e-mail + in-app).
12. Sistema exibe tela de confirmação com prazo de devolução.

**Fluxos Alternativos:**
- **4a. Livro com fila:** O sistema exibe o botão "Entrar na fila" em vez de "Reservar". O caso de uso continua em UC-005.
- **7a. Aluno inelegível:** O sistema exibe mensagem de erro específica ("Você possui atrasos pendentes", "Você está bloqueado", ou "Limite de 3 livros atingido").

**Pós-condições:**
- Exemplar está com status "alugado".
- Aluguel está com status "ativo".
- Aluno possui +1 livro alugado.

---

### UC-005 — Entrar na Fila de Espera

**Ator Principal:** Aluno  
**Pré-condições:**
- Aluno está autenticado.
- Livro não possui cópias disponíveis OU já possui fila de espera.
- Fila possui menos de 5 pessoas.

**Fluxo Principal:**
1. Aluno acessa a tela de busca e seleciona um livro indisponível.
2. Sistema exibe o botão "Entrar na fila".
3. Aluno clica em "Entrar na fila".
4. Sistema valida elegibilidade do aluno.
5. Sistema cria registro em FILA_ESPERA com posição = última + 1.
6. Sistema define status como "aguardando".
7. Sistema exibe confirmação com posição na fila.

**Fluxos Alternativos:**
- **2a. Fila cheia:** O sistema exibe o botão desabilitado com texto "Fila cheia".
- **4a. Aluno inelegível:** O sistema exibe mensagem de erro e não permite a entrada.

**Pós-condições:**
- Aluno está na fila de espera do livro.
- Posição na fila é registrada.

---

### UC-013 — Registrar Devolução

**Ator Principal:** Bibliotecário (ou Aluno via sistema)  
**Pré-condições:**
- O exemplar está com status "alugado".
- Existe um registro de ALUGUEL ativo para o exemplar.

**Fluxo Principal:**
1. Bibliotecário acessa a tela de devolução ou aluno inicia devolução pelo sistema.
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
- **3a. Livro não está emprestado:** O sistema alerta o usuário: "Este livro não está emprestado."
- **8a. Múltiplas cópias devolvidas:** Cada cópia notifica o próximo da fila individualmente, uma de cada vez.

**Pós-condições:**
- Aluguel está com status "devolvido".
- Exemplar está "disponível" ou "reservado" (para fila).
- Se houver atraso, multa foi gerada.

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
9. Sistema notifica o aluno (e-mail + in-app).

**Pós-condições:**
- Multa de dano registrada.
- Exemplar marcado como "danificado".

---

## 5. Regras de Negócio

### RN-001 — Prazo de Aluguel
- Livros pequenos (≤150 páginas): 15 dias.
- Livros grandes (>150 páginas): 30 dias.
- O tamanho é definido manualmente pelo bibliotecário no cadastro.

### RN-002 — Extensão de Prazo
- Máximo de 2 extensões por aluguel.
- 1ª extensão: +2 meses.
- 2ª extensão: +1 mês.
- Extensão só permitida se não houver fila de espera.
- Se alguém entrar na fila durante a extensão: devolução obrigatória em 3 dias.

### RN-003 — Fila de Espera
- Limite máximo: 5 pessoas por livro.
- Ordem: por chegada (FIFO).
- Cópias disponíveis vão para o primeiro da fila, mesmo que haja cópias livres.
- Se há fila, novos usuários só veem "Entrar na fila".

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
- Usuário com atraso ativo: impedido de alugar e entrar em fila.
- Usuário com 3 livros alugados: impedido de alugar e entrar em fila.
- Usuário bloqueado por penalidade: impedido até a data de desbloqueio.

### RN-007 — Notificações
- Canal duplo: e-mail + notificação in-app.
- Prazo de resposta na fila: 3 dias.
- Notificação de prazo próximo: 3 dias antes do vencimento.

---

## 6. Modelo de Dados Resumido

### Entidades Principais

```
USUARIO (id_usuario, nome, email, cpf, turma, senha_hash, tipo, bloqueado_ate, created_at)
LIVRO (id_livro, titulo, autor, genero, tamanho, num_paginas, valor_livro, created_at)
EXEMPLAR (id_exemplar, id_livro, codigo_barras, status, created_at)
ALUGUEL (id_aluguel, id_usuario, id_exemplar, data_aluguel, data_devolucao_prevista, data_devolucao_real, status, extensao_contador, data_extensao)
FILA_ESPERA (id_fila, id_livro, id_usuario, posicao, status, data_entrada, data_notificacao, data_limite_resposta)
MULTA (id_multa, id_aluguel, id_usuario, tipo, valor, dias_atraso, status, created_at)
```

### Relacionamentos
- USUARIO 1:N ALUGUEL
- USUARIO 1:N FILA_ESPERA
- LIVRO 1:N EXEMPLAR
- LIVRO 1:N FILA_ESPERA
- EXEMPLAR 1:N ALUGUEL
- ALUGUEL 1:N MULTA

---

## 7. Glossário

| Termo | Significado |
|-------|-------------|
| Aluguel | Registro de empréstimo de um exemplar para um usuário |
| Exemplar | Cópia física de um livro no acervo |
| Fila | Lista ordenada de espera por um livro |
| Extensão | Prorrogação do prazo de devolução |
| Multa | Penalidade financeira por atraso ou dano |
| Bloqueio | Suspensão temporária dos privilégios de aluguel |
| Notificação | Comunicação enviada ao usuário (e-mail + in-app) |

---

## 8. Histórico de Revisões

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-09-03 | Equipe Spec-Driven | Especificação inicial aprovada |

---

*Fim do documento*
