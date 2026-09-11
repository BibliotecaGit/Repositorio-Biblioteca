# Tarefas de Implementação — Sistema de Biblioteca
## Otimizado para IA de Programação (Google Jules / Cursor / Claude Code)

---

## Estratégia de Quebra

Cada tarefa abaixo é **atômica**, com **escopo fechado**, **critérios de aceitação testáveis** e **zero ambiguidade**. A ordem respeita dependências (não dá pra criar aluguel sem ter usuário e livro).

**Regras para a IA:**
- Implemente UMA tarefa por vez.
- Nunca pule dependências.
- Rode os testes/critérios de aceitação antes de marcar como concluído.
- Se encontrar conflito com a SPEC, pare e pergunte.

---

## Fase 0: Setup do Projeto

### T0.1 — Criar estrutura de pastas do projeto
**Dependências:** Nenhuma  
**Escopo:** Criar a árvore de diretórios do projeto.

```
/biblioteca/
├── /src/
│   ├── /models/          # Classes de entidade
│   ├── /controllers/     # Lógica de negócio
│   ├── /views/           # Interface (CLI ou Web)
│   ├── /database/        # Conexão e queries
│   ├── /services/        # Regras de negócio complexas
│   └── /utils/           # Funções auxiliares
├── /tests/               # Testes unitários e de integração
├── /docs/                # Documentação
├── /sql/                 # Scripts do banco
├── config.py             # Configurações gerais
├── requirements.txt      # Dependências Python
└── main.py               # Ponto de entrada
```

**Critérios de Aceitação:**
- [ ] Todas as pastas existem e estão vazias (ou com __init__.py).
- [ ] `requirements.txt` contém: `mysql-connector-python` (ou `pymysql`), `bcrypt`, `python-dotenv`.
- [ ] `config.py` lê variáveis de ambiente para host, user, password, database do MySQL.

---

### T0.2 — Conectar ao banco MySQL e criar schema
**Dependências:** T0.1  
**Escopo:** Script que conecta ao MySQL e executa o `schema_mysql.sql`.

**Critérios de Aceitação:**
- [ ] `database/connection.py` cria conexão usando `mysql-connector-python`.
- [ ] `database/setup.py` lê `sql/schema_mysql.sql` e executa no banco.
- [ ] Script roda sem erros e cria todas as 6 tabelas.
- [ ] Teste: `SHOW TABLES` retorna: usuario, livro, exemplar, aluguel, fila_espera, multa.

---

## Fase 1: Modelos (CRUD Básico)

### T1.1 — Modelo Usuario (CRUD completo)
**Dependências:** T0.2  
**Escopo:** Classe `Usuario` com create, read, update, delete.

**Atributos:** id_usuario, nome, email, cpf, turma, senha_hash, tipo, bloqueado_ate, created_at

**Critérios de Aceitação:**
- [ ] `models/usuario.py` com classe `Usuario`.
- [ ] `create()` insere no banco e retorna o ID gerado.
- [ ] `get_by_id(id)` retorna dicionário com dados do usuário.
- [ ] `get_by_cpf(cpf)` retorna usuário ou None.
- [ ] `update(id, **kwargs)` atualiza campos específicos.
- [ ] `delete(id)` remove do banco.
- [ ] `list_all()` retorna lista de todos os usuários.
- [ ] Teste: criar, buscar, atualizar nome, deletar, confirmar que sumiu.

---

### T1.2 — Modelo Livro (CRUD completo)
**Dependências:** T0.2  
**Escopo:** Classe `Livro` com create, read, update, delete.

**Atributos:** id_livro, titulo, autor, genero, tamanho, num_paginas, valor_livro, created_at

**Critérios de Aceitação:**
- [ ] `models/livro.py` com classe `Livro`.
- [ ] `create()` insere no banco. O campo `tamanho` é calculado automaticamente: ≤150 = 'pequeno', >150 = 'grande'.
- [ ] `get_by_id(id)` retorna dicionário.
- [ ] `search(query)` busca por titulo OU autor OU genero (LIKE %%query%%).
- [ ] `update(id, **kwargs)` atualiza campos. Se `num_paginas` mudar, recalcula `tamanho`.
- [ ] `delete(id)` remove do banco.
- [ ] Teste: criar livro com 200 páginas → tamanho deve ser 'grande'. Alterar para 100 → tamanho deve virar 'pequeno'.

---

### T1.3 — Modelo Exemplar (CRUD completo)
**Dependências:** T1.2  
**Escopo:** Classe `Exemplar` com create, read, update, delete.

**Atributos:** id_exemplar, id_livro, codigo_barras, status, created_at

**Critérios de Aceitação:**
- [ ] `models/exemplar.py` com classe `Exemplar`.
- [ ] `create()` insere com status 'disponivel' por padrão.
- [ ] `get_by_id(id)` retorna dicionário com dados do exemplar + titulo do livro (JOIN).
- [ ] `list_by_livro(id_livro)` retorna todos os exemplares de um livro.
- [ ] `update_status(id, novo_status)` altera status.
- [ ] Teste: criar 3 exemplares para um livro → listar e confirmar que retorna 3.

---

### T1.4 — Modelo Aluguel (CRUD completo)
**Dependências:** T1.1, T1.3  
**Escopo:** Classe `Aluguel` com create, read, update.

**Atributos:** id_aluguel, id_usuario, id_exemplar, data_aluguel, data_devolucao_prevista, data_devolucao_real, status, extensao_contador, data_extensao

**Critérios de Aceitação:**
- [ ] `models/aluguel.py` com classe `Aluguel`.
- [ ] `create(id_usuario, id_exemplar)` cria aluguel com status 'ativo' e data_devolucao_prevista calculada pela função `fn_calcular_prazo(id_livro)`.
- [ ] `get_by_id(id)` retorna dicionário com dados do aluguel + usuario + livro + exemplar.
- [ ] `list_by_usuario(id_usuario)` retorna todos os aluguéis de um usuário.
- [ ] `list_ativos()` retorna apenas aluguéis com status 'ativo' ou 'extensao'.
- [ ] `update_status(id, novo_status)` altera status.
- [ ] Teste: criar aluguel → confirmar que data_prevista é hoje + 15 dias (livro pequeno) ou + 30 dias (livro grande).

---

### T1.5 — Modelo FilaEspera (CRUD completo)
**Dependências:** T1.1, T1.2  
**Escopo:** Classe `FilaEspera` com create, read, update, delete.

**Atributos:** id_fila, id_livro, id_usuario, posicao, status, data_entrada, data_notificacao, data_limite_resposta

**Critérios de Aceitação:**
- [ ] `models/fila_espera.py` com classe `FilaEspera`.
- [ ] `create(id_livro, id_usuario)` insere com posição automática (última + 1) e status 'aguardando'.
- [ ] `get_by_id(id)` retorna dicionário.
- [ ] `list_by_livro(id_livro)` retorna fila ordenada por posição.
- [ ] `update_status(id, novo_status)` altera status.
- [ ] `delete(id)` remove da fila (desistência).
- [ ] Teste: adicionar 3 pessoas na fila → confirmar posições 1, 2, 3.

---

### T1.6 — Modelo Multa (CRUD completo)
**Dependências:** T1.4, T1.1  
**Escopo:** Classe `Multa` com create, read, update.

**Atributos:** id_multa, id_aluguel, id_usuario, tipo, valor, dias_atraso, status, created_at

**Critérios de Aceitação:**
- [ ] `models/multa.py` com classe `Multa`.
- [ ] `create(id_aluguel, id_usuario, tipo, valor, dias_atraso=None)` insere multa.
- [ ] `get_by_id(id)` retorna dicionário.
- [ ] `list_by_usuario(id_usuario)` retorna multas do usuário.
- [ ] `list_pendentes()` retorna multas com status 'pendente'.
- [ ] `pagar(id)` altera status para 'pago'.
- [ ] Teste: criar multa de R$ 15,00 → listar pendentes → pagar → confirmar status 'pago'.

---

## Fase 2: Regras de Negócio (Services)

### T2.1 — Service: Elegibilidade do Usuário
**Dependências:** T1.1, T1.4, T1.5  
**Escopo:** Função que verifica se usuário pode alugar ou entrar na fila.

**Regras da SPEC:**
- Não pode ter atraso ativo (aluguel com status 'atrasado').
- Não pode estar bloqueado (bloqueado_ate > agora).
- Não pode ter 3+ livros alugados (status 'ativo' ou 'extensao').

**Critérios de Aceitação:**
- [ ] `services/elegibilidade.py` com função `verificar_elegibilidade(id_usuario)`.
- [ ] Retorna `{'elegivel': True}` ou `{'elegivel': False, 'motivo': '...'}`.
- [ ] Teste: usuário sem aluguel → elegível.
- [ ] Teste: usuário com 3 aluguéis ativos → não elegível (motivo: "Limite de 3 livros atingido").
- [ ] Teste: usuário com atraso → não elegível (motivo: "Possui atrasos pendentes").
- [ ] Teste: usuário bloqueado → não elegível (motivo: "Usuário bloqueado até DD/MM/YYYY").

---

### T2.2 — Service: Calcular Prazo de Devolução
**Dependências:** T1.2  
**Escopo:** Função que calcula data de devolução baseada no tamanho do livro.

**Regras da SPEC:**
- Pequeno (≤150 páginas): hoje + 15 dias.
- Grande (>150 páginas): hoje + 30 dias.

**Critérios de Aceitação:**
- [ ] `services/prazo.py` com função `calcular_prazo(id_livro)`.
- [ ] Retorna `datetime` com a data calculada.
- [ ] Teste: livro com 100 páginas → prazo = hoje + 15 dias.
- [ ] Teste: livro com 200 páginas → prazo = hoje + 30 dias.

---

### T2.3 — Service: Alugar Livro (com todas as validações)
**Dependências:** T2.1, T2.2, T1.4, T1.3, T1.5  
**Escopo:** Função completa que executa o aluguel com todas as regras.

**Regras da SPEC:**
- Usuário deve estar autenticado (login feito).
- Usuário deve ser elegível (T2.1).
- Livro deve ter exemplar disponível E não ter fila.
- Se há fila, não pode alugar direto (deve entrar na fila).
- Ao alugar, exemplar muda para 'alugado'.
- Criar registro de aluguel com prazo calculado.

**Critérios de Aceitação:**
- [ ] `services/aluguel_service.py` com função `alugar_livro(id_usuario, id_livro)`.
- [ ] Verifica elegibilidade do usuário. Se não elegível, retorna erro com motivo.
- [ ] Verifica se há fila para o livro. Se sim, retorna erro: "Livro possui fila de espera. Entre na fila."
- [ ] Busca um exemplar disponível do livro. Se não houver, retorna erro.
- [ ] Cria aluguel com status 'ativo' e prazo calculado.
- [ ] Altera status do exemplar para 'alugado'.
- [ ] Retorna sucesso com dados do aluguel.
- [ ] Teste: alugar livro disponível sem fila → sucesso.
- [ ] Teste: alugar livro com fila → erro.
- [ ] Teste: alugar com usuário inelegível → erro.

---

### T2.4 — Service: Entrar na Fila de Espera
**Dependências:** T2.1, T1.5  
**Escopo:** Função que adiciona usuário na fila com todas as validações.

**Regras da SPEC:**
- Usuário deve ser elegível.
- Fila não pode ter 5+ pessoas.
- Posição é automática (última + 1).

**Critérios de Aceitação:**
- [ ] `services/fila_service.py` com função `entrar_fila(id_usuario, id_livro)`.
- [ ] Verifica elegibilidade. Se não elegível, retorna erro.
- [ ] Conta pessoas na fila. Se ≥ 5, retorna erro: "Fila cheia".
- [ ] Cria registro na fila com posição automática e status 'aguardando'.
- [ ] Retorna sucesso com posição na fila.
- [ ] Teste: entrar na fila de livro com 4 pessoas → sucesso, posição 5.
- [ ] Teste: entrar na fila de livro com 5 pessoas → erro "Fila cheia".

---

### T2.5 — Service: Devolver Livro e Processar Fila
**Dependências:** T2.3, T1.4, T1.5, T1.3  
**Escopo:** Função que processa devolução, calcula multa e notifica fila.

**Regras da SPEC:**
- Se livro não está emprestado → alerta erro.
- Se há atraso → calcula multa (R$ 3/dia, teto = valor do livro).
- Se há fila → notifica próximo (status 'notificado', data_limite = hoje + 3 dias).
- Se não há fila → exemplar fica 'disponivel'.
- Se múltiplas cópias devolvidas → cada uma notifica o próximo individualmente.

**Critérios de Aceitação:**
- [ ] `services/devolucao_service.py` com função `devolver_livro(id_aluguel)`.
- [ ] Verifica se aluguel existe e está ativo/extensao. Se não, erro.
- [ ] Calcula atraso: se data_real > prevista, calcula multa e cria registro em MULTA.
- [ ] Atualiza aluguel para status 'devolvido' e data_devolucao_real = agora.
- [ ] Verifica fila. Se há fila:
  - Pega primeiro da fila (status 'aguardando').
  - Atualiza exemplar para 'reservado'.
  - Atualiza fila: status 'notificado', data_notificacao = agora, data_limite = agora + 3 dias.
- [ ] Se não há fila: exemplar vira 'disponivel'.
- [ ] Retorna sucesso com informações do que aconteceu.
- [ ] Teste: devolver livro sem fila e sem atraso → exemplar fica disponível.
- [ ] Teste: devolver livro com atraso de 5 dias → multa de R$ 15,00 criada.
- [ ] Teste: devolver livro com fila → primeiro da fila é notificado.

---

### T2.6 — Service: Extensão de Prazo
**Dependências:** T2.3, T1.4, T1.5  
**Escopo:** Função que permite usuário solicitar extensão do prazo.

**Regras da SPEC:**
- Só permite se não houver fila para o livro.
- Máximo 2 extensões (contador 0→1→2).
- 1ª extensão: +2 meses.
- 2ª extensão: +1 mês.
- Se alguém entra na fila durante extensão → notifica usuário, 3 dias para devolver.

**Critérios de Aceitação:**
- [ ] `services/extensao_service.py` com função `solicitar_extensao(id_aluguel)`.
- [ ] Verifica se aluguel está ativo/extensao. Se devolvido/atrasado → erro.
- [ ] Verifica contador de extensões. Se ≥ 2 → erro: "Limite de extensões atingido".
- [ ] Verifica se há fila para o livro. Se sim → erro: "Não é possível estender. Há pessoas na fila."
- [ ] Se contador = 0: data_devolucao = agora + 2 meses, contador = 1.
- [ ] Se contador = 1: data_devolucao = agora + 1 mês, contador = 2.
- [ ] Atualiza status do aluguel para 'extensao'.
- [ ] Retorna sucesso com nova data.
- [ ] Teste: solicitar 1ª extensão → data + 2 meses, contador = 1.
- [ ] Teste: solicitar 3ª extensão → erro.
- [ ] Teste: solicitar extensão com fila → erro.

---

### T2.7 — Service: Confirmar Aluguel da Fila
**Dependências:** T2.3, T1.5, T1.4  
**Escopo:** Função que confirma aluguel quando usuário é chamado da fila.

**Regras da SPEC:**
- Usuário tem 3 dias para confirmar (data_limite_resposta).
- Se confirmar → cria novo aluguel, exemplar vira 'alugado'.
- Se não confirmar em 3 dias → status 'expirado', notifica próximo.
- Se desistir → status 'desistiu', notifica próximo imediatamente.

**Critérios de Aceitação:**
- [ ] `services/fila_service.py` com funções `confirmar_aluguel_fila(id_fila)` e `desistir_fila(id_fila)`.
- [ ] `confirmar_aluguel_fila`: verifica se status é 'notificado' e se ainda está dentro do prazo. Se expirado → erro.
- [ ] Cria novo aluguel para o usuário com o exemplar reservado.
- [ ] Atualiza fila para status 'confirmado'.
- [ ] Altera exemplar para 'alugado'.
- [ ] `desistir_fila`: atualiza status para 'desistiu' e chama função de notificar próximo.
- [ ] Teste: confirmar dentro do prazo → aluguel criado.
- [ ] Teste: confirmar após expiração → erro.

---

### T2.8 — Service: Calcular e Aplicar Multa por Atraso (Job Diário)
**Dependências:** T1.4, T1.6  
**Escopo:** Função que roda diariamente para verificar atrasos e aplicar multas/bloqueios.

**Regras da SPEC:**
- Verifica aluguéis com data_prevista < hoje e status ativo/extensao.
- Calcula dias de atraso.
- Multa = dias × R$ 3,00 (teto = valor do livro).
- Se atraso > 15 dias: bloqueio de 1 semana.
- Se atingiu teto e continua atrasando: +1 dia de bloqueio por dia.

**Critérios de Aceitação:**
- [ ] `services/multa_service.py` com função `verificar_atrasos()`.
- [ ] Busca aluguéis ativos/extensao com data_prevista < agora.
- [ ] Para cada um:
  - Calcula dias_atraso = DATEDIFF(agora, data_prevista).
  - Se > 0: muda status para 'atrasado'.
  - Calcula multa = MIN(dias × 3.00, valor_livro).
  - Insere multa se não existir (evita duplicar).
  - Se dias > 15: calcula bloqueio = 7 dias + max(0, dias - (valor/3)).
  - Atualiza usuario.bloqueado_ate.
- [ ] Teste: simular aluguel atrasado 5 dias → multa de R$ 15 criada.
- [ ] Teste: simular aluguel atrasado 20 dias → multa de R$ 60 (ou teto) + bloqueio calculado.

---

### T2.9 — Service: Aplicar Multa por Dano
**Dependências:** T1.6, T1.4  
**Escopo:** Função para bibliotecário aplicar multa de dano.

**Regras da SPEC:**
- Valor fixo: R$ 20,00.
- Aplicada manualmente pelo bibliotecário.
- Cumulativa com multa de atraso.

**Critérios de Aceitação:**
- [ ] `services/multa_service.py` com função `aplicar_multa_dano(id_aluguel, tipo_dano)`.
- [ ] Verifica se aluguel existe e foi devolvido.
- [ ] Cria multa com tipo 'dano', valor 20.00.
- [ ] Atualiza exemplar para status 'danificado'.
- [ ] Retorna sucesso.
- [ ] Teste: aplicar multa de dano → multa criada, exemplar = 'danificado'.

---

## Fase 3: Interface do Usuário (CLI ou Web)

### T3.1 — Tela de Login
**Dependências:** T1.1  
**Escopo:** Interface para autenticação.

**Critérios de Aceitação:**
- [ ] Solicita CPF e senha.
- [ ] Valida credenciais (comparar hash com bcrypt).
- [ ] Se válido: armazena sessão (id_usuario, tipo) e redireciona para menu principal.
- [ ] Se inválido: mensagem de erro genérica.
- [ ] Teste: login com credenciais corretas → sucesso.
- [ ] Teste: login com senha errada → erro.

---

### T3.2 — Menu Principal por Perfil
**Dependências:** T3.1  
**Escopo:** Menu adaptativo baseado no tipo de usuário.

**Critérios de Aceitação:**
- [ ] Aluno vê: Buscar Livros, Meus Aluguéis, Minha Fila, Minhas Multas, Sair.
- [ ] Bibliotecário vê: Gerenciar Acervo, Registrar Devolução, Aplicar Multa Dano, Visualizar Filas/Atrasos, Sair.
- [ ] Administrador vê: Categorizar Títulos, Excluir Títulos, Gerenciar Usuários, Sair.
- [ ] Teste: logar como cada tipo e confirmar menu correto.

---

### T3.3 — Tela: Buscar e Alugar Livro (Aluno)
**Dependências:** T3.2, T2.3, T2.4  
**Escopo:** Busca livros e exibe botão correto (Reservar / Entrar na fila / Fila cheia).

**Critérios de Aceitação:**
- [ ] Campo de busca com resultado em lista.
- [ ] Cada livro mostra: título, autor, gênero, páginas, cópias disponíveis, pessoas na fila.
- [ ] Se cópias > 0 E fila = 0 → botão "Reservar".
- [ ] Se cópias = 0 OU fila > 0 E fila < 5 → botão "Entrar na fila".
- [ ] Se fila = 5 → botão desabilitado "Fila cheia".
- [ ] Ao clicar, executa ação e mostra resultado.
- [ ] Teste: buscar "Dom Casmurro" → 3 cópias, 0 fila → botão Reservar funciona.

---

### T3.4 — Tela: Meus Aluguéis (Aluno)
**Dependências:** T3.2, T1.4, T2.6  
**Escopo:** Lista aluguéis ativos com opção de extensão e devolução.

**Critérios de Aceitação:**
- [ ] Lista aluguéis ativos com: título, data aluguel, data prevista, status.
- [ ] Se em dia: botão "Solicitar extensão" (se contador < 2 e sem fila).
- [ ] Se em extensão: mostra "1ª extensão ativa" ou "2ª extensão ativa".
- [ ] Se atrasado: destaque vermelho com valor da multa + botão "Devolver agora".
- [ ] Teste: aluno com 2 aluguéis → lista correta, extensão funciona.

---

### T3.5 — Tela: Notificação da Fila (Aluno)
**Dependências:** T3.2, T2.7  
**Escopo:** Quando usuário é chamado da fila, mostra tela de confirmação.

**Critérios de Aceitação:**
- [ ] Mostra: "É a sua vez na fila!", nome do livro, prazo de 3 dias.
- [ ] Botões: "Confirmar aluguel" e "Desistir".
- [ ] Mostra contador de expiração.
- [ ] Teste: confirmar → aluguel criado.
- [ ] Teste: desistir → próximo da fila é notificado.

---

### T3.6 — Tela: Gerenciar Acervo (Bibliotecário)
**Dependências:** T3.2, T1.2, T1.3  
**Escopo:** CRUD de livros e exemplares.

**Critérios de Aceitação:**
- [ ] Lista todos os livros com: título, autor, gênero, tamanho, total de cópias.
- [ ] Botão "Novo livro" com formulário (título, autor, gênero, páginas, valor).
- [ ] Botão "Adicionar exemplar" para um livro existente.
- [ ] Botão "Editar" para alterar dados do livro.
- [ ] Teste: criar livro com 200 páginas → tamanho automático = 'grande'.

---

### T3.7 — Tela: Registrar Devolução (Bibliotecário)
**Dependências:** T3.2, T2.5  
**Escopo:** Processa devolução física e aplica multa de dano se necessário.

**Critérios de Aceitação:**
- [ ] Campo para buscar aluguel por ID ou código de barras do exemplar.
- [ ] Mostra dados do aluguel + usuário + livro.
- [ ] Se devolução normal: botão "Confirmar devolução".
- [ ] Se há dano: checkbox "Aplicar multa por dano" + dropdown de tipo de dano.
- [ ] Ao confirmar: processa devolução (T2.5) e, se dano, aplica multa (T2.9).
- [ ] Teste: devolver sem dano → exemplar disponível ou fila notificada.
- [ ] Teste: devolver com dano → multa de R$ 20 criada.

---

### T3.8 — Tela: Categorizar Títulos (Administrador)
**Dependências:** T3.2, T1.2  
**Escopo:** Permite admin alterar gênero e tamanho dos livros.

**Critérios de Aceitação:**
- [ ] Lista todos os livros.
- [ ] Edição inline de gênero e tamanho.
- [ ] Alteração de tamanho manual sobrescreve a regra automática.
- [ ] Teste: alterar gênero de "Romance" para "Clássico" → salvo corretamente.

---

## Fase 4: Testes de Integração

### T4.1 — Teste: Fluxo Completo Aluguel → Devolução → Fila
**Dependências:** Todas as anteriores  
**Escopo:** Simula o ciclo completo.

**Passos:**
1. Cadastrar usuário A e usuário B.
2. Cadastrar livro com 1 exemplar.
3. Usuário A aluga o livro.
4. Usuário B entra na fila (posição 1).
5. Usuário A devolve o livro.
6. Verificar: exemplar fica 'reservado', usuário B é notificado.
7. Usuário B confirma aluguel.
8. Verificar: novo aluguel criado para B, exemplar 'alugado'.

**Critérios de Aceitação:**
- [ ] Todo o fluxo executa sem erros.
- [ ] Estados intermediários estão corretos no banco.

---

### T4.2 — Teste: Fluxo de Extensão Interrompida por Fila
**Dependências:** Todas as anteriores  
**Escopo:** Simula extensão que é interrompida.

**Passos:**
1. Usuário A aluga livro.
2. Usuário A solicita extensão (contador = 1).
3. Usuário B entra na fila.
4. Verificar: usuário A é notificado, tem 3 dias para devolver.
5. Usuário A não devolve em 3 dias.
6. Verificar: multa de atraso é calculada e aplicada.

**Critérios de Aceitação:**
- [ ] Notificação de devolução obrigatória é gerada.
- [ ] Após 3 dias, multa é criada.
- [ ] Usuário A fica bloqueado.

---

### T4.3 — Teste: Job de Multa e Bloqueio
**Dependências:** T2.8  
**Escopo:** Simula atraso de 20 dias e verifica multa + bloqueio.

**Passos:**
1. Criar aluguel com data_prevista = hoje - 20 dias.
2. Executar `verificar_atrasos()`.
3. Verificar multa criada.
4. Verificar bloqueio calculado.

**Critérios de Aceitação:**
- [ ] Multa = MIN(20 × 3, valor_livro).
- [ ] Bloqueio = 7 dias + (20 - valor/3) dias extras se atingiu teto.
- [ ] Usuário não consegue alugar nem entrar em fila.

---

## Ordem de Execução Recomendada

```
Fase 0:  T0.1 → T0.2
Fase 1:  T1.1 → T1.2 → T1.3 → T1.4 → T1.5 → T1.6
Fase 2:  T2.1 → T2.2 → T2.3 → T2.4 → T2.5 → T2.6 → T2.7 → T2.8 → T2.9
Fase 3:  T3.1 → T3.2 → T3.3 → T3.4 → T3.5 → T3.6 → T3.7 → T3.8
Fase 4:  T4.1 → T4.2 → T4.3
```

---

## Convenções de Código para a IA

### Nomenclatura
- Classes: `PascalCase` (ex: `Usuario`, `AluguelService`)
- Funções/variáveis: `snake_case` (ex: `calcular_prazo`, `id_usuario`)
- Constantes: `UPPER_SNAKE_CASE` (ex: `LIMITE_FILA = 5`)
- Arquivos: `snake_case.py` (ex: `usuario.py`, `aluguel_service.py`)

### Estrutura de Arquivo Padrão
```python
# models/usuario.py
from database.connection import get_connection

class Usuario:
    def __init__(self, nome, email, cpf, turma, senha_hash, tipo='aluno'):
        self.nome = nome
        self.email = email
        # ...

    def create(self):
        # INSERT no banco
        pass

    @staticmethod
    def get_by_id(id_usuario):
        # SELECT por ID
        pass
```

### Tratamento de Erros
- Sempre retornar dicionário: `{'sucesso': True, 'dados': ...}` ou `{'sucesso': False, 'erro': 'mensagem'}`.
- Nunca deixar exceções não tratadas subirem para o usuário.

### Banco de Dados
- Usar prepared statements (`%s` placeholders) para evitar SQL Injection.
- Fechar conexões e cursores em `finally` blocks.

---

## Checklist Final de Entrega

- [ ] Todas as 6 tabelas criadas e populáveis.
- [ ] Todos os 18 casos de uso implementados.
- [ ] Todas as 7 regras de negócio funcionando.
- [ ] Testes de integração T4.1, T4.2, T4.3 passando.
- [ ] Nenhuma exceção não tratada.
- [ ] Código comentado em português.

---

*Documento gerado para execução por IA de programação. Baseado na SPEC v1.0.*

