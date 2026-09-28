-- ============================================================================
-- AURORA — Sistema de Biblioteca com Aluguel de Livros
-- Schema para Supabase (PostgreSQL 15+) — adaptado da Spec v3.0 (§6.3)
-- Gerado em: 2026-09-28
--
-- NOTAS DE ADAPTAÇÃO MySQL → Postgres/Supabase:
--   * ENGINE InnoDB / utf8mb4  → Postgres nativo (UTF-8)
--   * BIGINT UNSIGNED AUTO_INCREMENT → BIGSERIAL
--   * DATETIME                 → TIMESTAMPTZ
--   * ENUM(...)                → CREATE TYPE ... AS ENUM
--   * ON DELETE RESTRICT       → Postgres default (sem ON DELETE = RESTRICT)
--   * Triggers p/ avaliacao_media, updated_at e faixa de tamanho inclusos
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 0. EXTENSÕES
-- ----------------------------------------------------------------------------
create extension if not exists "pgcrypto";  -- gen_random_uuid(), se necessário

-- ----------------------------------------------------------------------------
-- 1. ENUMS (§6.2 — máquinas de estado consolidadas)
-- ----------------------------------------------------------------------------
create type usuario_tipo     as enum ('aluno', 'bibliotecario', 'administrador');
create type usuario_idioma   as enum ('pt-BR', 'en');
create type usuario_tema     as enum ('claro', 'escuro');
create type faixa_tamanho    as enum ('pequeno', 'medio_pequeno', 'medio_padrao', 'grande');
create type exemplar_status  as enum ('disponivel', 'alugado', 'danificado', 'manutencao');
create type aluguel_status   as enum ('ativo', 'devolvido');
create type multa_tipo       as enum ('atraso', 'dano');
create type multa_status     as enum ('pendente', 'pago');
create type tipo_filtro      as enum ('tamanho', 'genero', 'autor');

-- ----------------------------------------------------------------------------
-- 2. TABELAS
-- ----------------------------------------------------------------------------

-- USUARIO -------------------------------------------------------------------
create table usuario (
  id_usuario        bigint generated always as identity primary key,
  nome              varchar(150) not null,
  email             varchar(190) not null unique,               -- RF-001/002 (RNF-007)
  senha_hash        text not null,                              -- bcrypt/argon2 (RNF-005)
  tipo              usuario_tipo not null default 'aluno',      -- CA-001.4
  foto_url          varchar(500),
  idioma            usuario_idioma not null default 'pt-BR',
  tema              usuario_tema  not null default 'claro',
  uf                char(2),                                    -- região (RF-028)
  cidade            varchar(120),                               -- região (RF-028)
  end_logradouro    varchar(190),
  end_numero        varchar(20),
  end_complemento   varchar(120),
  end_bairro        varchar(120),
  end_cidade        varchar(120),
  end_uf            char(2),
  end_cep           varchar(8),                                 -- só dígitos; formato validado na app (RF-025)
  cartao_token      text,                                       -- tokenizado (RNF-008)
  cartao_mascarado  varchar(25),                                -- ex.: '**** 1234'
  bloqueado_ate     date,                                       -- RF-013.5 (RN-006)
  deleted_at        timestamptz,                                -- soft delete
  created_at        timestamptz not null default now(),
  updated_at        timestamptz not null default now()
);

-- LIVRO ---------------------------------------------------------------------
create table livro (
  id_livro            bigint generated always as identity primary key,
  titulo              varchar(255) not null,
  autor               varchar(190) not null,
  genero              varchar(80) not null,
  faixa_tamanho       faixa_tamanho not null,                   -- RN-001 (auto via trigger)
  tamanho_manual      boolean not null default false,           -- CA-008.2: admin sobrescreve a regra
  num_paginas         integer not null check (num_paginas > 0),
  valor_livro         numeric(10,2) not null check (valor_livro >= 0),     -- teto da multa (RN-004)
  preco_aluguel       numeric(10,2) not null check (preco_aluguel >= 0),   -- RN-008
  capa_url            varchar(500),
  data_lancamento     date,                                     -- alimenta Novidades (RN-011)
  avaliacao_media     numeric(3,2) not null default 0 check (avaliacao_media between 0 and 5),  -- denormalizado (RN-009)
  total_avaliacoes    integer not null default 0 check (total_avaliacoes >= 0),
  alugueis_ultimo_ano integer not null default 0 check (alugueis_ultimo_ano >= 0),              -- Destaques (RN-011)
  eh_infantil         boolean not null default false,           -- CA-008.3 (gênero 'Infantil')
  deleted_at          timestamptz,                              -- RF-007.2 / INT-6
  created_at          timestamptz not null default now(),
  updated_at          timestamptz not null default now()
);

-- EXEMPLAR ------------------------------------------------------------------
create table exemplar (
  id_exemplar    bigint generated always as identity primary key,
  id_livro       bigint not null references livro (id_livro) on delete restrict,  -- RF-007.1 / INT-5
  codigo_barras  varchar(64) unique,                            -- CA-005.2 (opcional)
  status         exemplar_status not null default 'disponivel', -- CA-005.3
  created_at     timestamptz not null default now(),
  updated_at     timestamptz not null default now()
);

-- ALUGUEL -------------------------------------------------------------------
create table aluguel (
  id_aluguel              bigint generated always as identity primary key,
  id_usuario              bigint not null references usuario  (id_usuario)  on delete restrict,
  id_exemplar             bigint not null references exemplar (id_exemplar) on delete restrict,
  data_aluguel            timestamptz not null default now(),
  data_devolucao_prevista date not null,                        -- hoje+15 ou hoje+30 (RN-001)
  data_devolucao_real     date,
  status                  aluguel_status not null default 'ativo',
  extensao_contador       smallint not null default 0 check (extensao_contador between 0 and 2),  -- RN-002
  preco_cobrado           numeric(10,2) not null,               -- preco_aluguel no momento do aluguel
  created_at              timestamptz not null default now(),
  updated_at              timestamptz not null default now()
);

-- JA_LIDOS ------------------------------------------------------------------
create table ja_lidos (
  id             bigint generated always as identity primary key,
  id_usuario     bigint not null references usuario (id_usuario) on delete cascade,
  id_livro       bigint not null references livro   (id_livro)   on delete cascade,
  data_marcacao  timestamptz not null default now(),
  unique (id_usuario, id_livro)                                 -- CA-021
);

-- LISTA_DESEJOS -------------------------------------------------------------
create table lista_desejos (
  id             bigint generated always as identity primary key,
  id_usuario     bigint not null references usuario (id_usuario) on delete cascade,
  id_livro       bigint not null references livro   (id_livro)   on delete cascade,
  data_marcacao  timestamptz not null default now(),
  unique (id_usuario, id_livro)                                 -- CA-022
);

-- AVALIACAO -----------------------------------------------------------------
create table avaliacao (
  id_avaliacao   bigint generated always as identity primary key,
  id_usuario     bigint not null references usuario (id_usuario) on delete cascade,
  id_livro       bigint not null references livro   (id_livro)   on delete cascade,
  nota           numeric(2,1) not null check (nota between 0 and 5),  -- RN-009
  data_avaliacao timestamptz not null default now(),
  unique (id_usuario, id_livro)                                 -- RN-009: uma avaliação por livro (editável)
);

-- USO_FILTRO ----------------------------------------------------------------
create table uso_filtro (
  id          bigint generated always as identity primary key,
  id_usuario  bigint not null references usuario (id_usuario) on delete cascade,
  tipo_filtro tipo_filtro not null,
  valor       varchar(120) not null,
  contagem    integer not null default 0 check (contagem >= 0),
  updated_at  timestamptz not null default now(),
  unique (id_usuario, tipo_filtro, valor)                       -- CA-020.1
);

-- MULTA ---------------------------------------------------------------------
create table multa (
  id_multa        bigint generated always as identity primary key,
  id_aluguel      bigint not null references aluguel (id_aluguel) on delete restrict,
  id_usuario      bigint not null references usuario (id_usuario) on delete restrict,
  tipo            multa_tipo not null,
  valor           numeric(10,2) not null check (valor > 0),
  dias_atraso     smallint check (dias_atraso is null or dias_atraso >= 0),
  status          multa_status not null default 'pendente',
  data_pagamento  timestamptz,
  created_at      timestamptz not null default now()
);

-- CONTATO_SUPORTE (RF-029.3 — pendência de decisão da spec, tabela proposta) --
create table contato_suporte (
  id            bigint generated always as identity primary key,
  id_usuario    bigint references usuario (id_usuario) on delete set null,
  email         varchar(190) not null,
  assunto       varchar(190) not null,
  mensagem      text not null,
  respondido    boolean not null default false,
  created_at    timestamptz not null default now()
);

-- CONFIGURACAO (texto institucional editável — pendência de decisão, CA-018.1)
create table configuracao (
  chave       varchar(80) primary key,
  valor       text not null,
  updated_at  timestamptz not null default now()
);

insert into configuracao (chave, valor) values
  ('descricao_institucional',
   'A Aurora é uma biblioteca comunitária de aluguel de livros, com a missão de incentivar a leitura. Nosso acervo possui 3 cópias por título.');

-- ----------------------------------------------------------------------------
-- 3. ÍNDICES (§6.4 — com justificativas em comentário)
-- ----------------------------------------------------------------------------
create index idx_livro_titulo           on livro (titulo);                    -- busca parcial vitrine (RF-009)
create index idx_livro_autor            on livro (autor);
create index idx_livro_genero           on livro (genero);
create index idx_livro_faixa_tamanho    on livro (faixa_tamanho);             -- filtro Por Tamanho (RF-019)
create index idx_livro_data_lancamento  on livro (data_lancamento);           -- Novidades (RN-011)
create index idx_livro_eh_infantil      on livro (eh_infantil);               -- Infantis (RF-019)
create index idx_livro_avaliacao_media  on livro (avaliacao_media);           -- Destaques (RN-011)
create index idx_livro_alugueis_1ano    on livro (alugueis_ultimo_ano);       -- Destaques (RN-011)
create index idx_exemplar_livro_status  on exemplar (id_livro, status);       -- disponibilidade das 3 cópias (RF-010)
create index idx_aluguel_data_prevista  on aluguel (data_devolucao_prevista); -- job diário de atrasos (RF-013.1/RNF-003)
create index idx_aluguel_usuario_status on aluguel (id_usuario, status);      -- elegibilidade < 3 ativos (RN-006)
create index idx_multa_usuario_status   on multa (id_usuario, status);        -- bloqueio por multa pendente (RN-006) + tela RF-015

-- Opcional (escala): busca full-text em título/autor
-- create index idx_livro_fts on livro using gin (to_tsvector('portuguese', titulo || ' ' || autor));

-- ----------------------------------------------------------------------------
-- 4. FUNÇÕES E TRIGGERS
-- ----------------------------------------------------------------------------

-- 4.1 updated_at automático ---------------------------------------------------
create or replace function fn_touch_updated_at() returns trigger as $$
begin
  new.updated_at := now();
  return new;
end;
$$ language plpgsql;

create trigger trg_usuario_updated  before update on usuario  for each row execute function fn_touch_updated_at();
create trigger trg_livro_updated    before update on livro    for each row execute function fn_touch_updated_at();
create trigger trg_exemplar_updated before update on exemplar for each row execute function fn_touch_updated_at();
create trigger trg_aluguel_updated  before update on aluguel  for each row execute function fn_touch_updated_at();
create trigger trg_uso_filtro_updated before update on uso_filtro for each row execute function fn_touch_updated_at();

-- 4.2 Faixa de tamanho automática (RN-001 / CA-004.2) -------------------------
-- Regra: até 100 pág = pequeno | >100–150 = medio_pequeno
--        >150–250 = medio_padrao | >250 = grande
create or replace function calcular_faixa_tamanho(p_paginas integer) returns faixa_tamanho as $$
begin
  if p_paginas <= 100 then return 'pequeno';
  elsif p_paginas <= 150 then return 'medio_pequeno';
  elsif p_paginas <= 250 then return 'medio_padrao';
  else return 'grande';
  end if;
end;
$$ language plpgsql immutable;

create or replace function fn_livro_faixa_tamanho() returns trigger as $$
begin
  if (tg_op = 'INSERT') or (new.num_paginas is distinct from old.num_paginas) then
    if not new.tamanho_manual then                       -- respeita override do admin (CA-008.2)
      new.faixa_tamanho := calcular_faixa_tamanho(new.num_paginas);
    end if;
  end if;
  if new.genero ilike 'infantil' then                    -- CA-008.3
    new.eh_infantil := true;
  else
    new.eh_infantil := false;
  end if;
  return new;
end;
$$ language plpgsql;

create trigger trg_livro_faixa_tamanho
  before insert or update of num_paginas, genero on livro
  for each row execute function fn_livro_faixa_tamanho();

-- 4.3 Recálculo de avaliacao_media (RN-009 / RNF-004 — denormalização §6.6) ---
create or replace function fn_recalc_avaliacao() returns trigger as $$
declare
  v_livro_id bigint := coalesce(new.id_livro, old.id_livro);
begin
  update livro l
     set avaliacao_media  = coalesce((select round(avg(a.nota)::numeric, 2) from avaliacao a where a.id_livro = v_livro_id), 0),
         total_avaliacoes =        (select count(*)                        from avaliacao a where a.id_livro = v_livro_id)
   where l.id_livro = v_livro_id;
  return coalesce(new, old);
end;
$$ language plpgsql;

create trigger trg_avaliacao_recalc
  after insert or update or delete on avaliacao
  for each row execute function fn_recalc_avaliacao();

-- ----------------------------------------------------------------------------
-- 5. VIEWS DE NEGÓCIO
-- ----------------------------------------------------------------------------

-- Novidades (RN-011): lançamentos dos últimos 6 meses, catálogo ativo
create or replace view vw_novidades as
select l.*
  from livro l
 where l.deleted_at is null
   and l.data_lancamento >= current_date - interval '6 months'
 order by l.data_lancamento desc;

-- Destaques (RN-011): avaliação >= 4,5 E aluguéis no último ano >= percentil 80
create or replace view vw_destaques as
with percentil as (
  select percentile_disc(0.8) within group (order by alugueis_ultimo_ano) as p80
    from livro
   where deleted_at is null
)
select l.*
  from livro l, percentil p
 where l.deleted_at is null
   and l.avaliacao_media >= 4.5
   and l.alugueis_ultimo_ano >= p.p80
 order by l.alugueis_ultimo_ano desc;

-- ----------------------------------------------------------------------------
-- 6. FUNÇÕES AUXILIARES DE NEGÓCIO (usadas pela aplicação/jobs)
-- ----------------------------------------------------------------------------

-- RN-001: prazo de aluguel em dias (15 ou 30)
create or replace function prazo_aluguel_dias(p_faixa faixa_tamanho) returns integer as $$
begin
  if p_faixa in ('pequeno', 'medio_pequeno') then return 15; else return 30; end if;
end;
$$ language plpgsql immutable;

-- RN-004: multa por atraso com teto = valor do livro
create or replace function calcular_multa_atraso(p_dias_atraso integer, p_valor_livro numeric) returns numeric as $$
begin
  return least(p_dias_atraso * 3.00, p_valor_livro);  -- R$ 3,00/dia, teto = valor_livro
end;
$$ language plpgsql immutable;

-- ----------------------------------------------------------------------------
-- 7. SEEDS MÍNIMOS (opcional — descomente para popular)
-- ----------------------------------------------------------------------------
-- insert into usuario (nome, email, senha_hash, tipo) values
--   ('Admin Aurora',    'admin@aurora.local',    '<hash bcrypt>', 'administrador'),
--   ('Biblio Aurora',   'biblio@aurora.local',   '<hash bcrypt>', 'bibliotecario'),
--   ('Aluno Teste',     'aluno@aurora.local',    '<hash bcrypt>', 'aluno');

-- ----------------------------------------------------------------------------
-- 8. ROW LEVEL SECURITY (Supabase) — HABILITE CONFORME A ARQUITETURA DE AUTH
-- ----------------------------------------------------------------------------
-- Se usar auth.users do Supabase, crie uma coluna auth_user_id uuid unique em
-- "usuario" e vincule as políticas abaixo a auth.uid().
-- Exemplo de política para coleções pessoais:
--
-- alter table ja_lidos enable row level security;
-- create policy "usuario_gerencia_proprios_lidos"
--   on ja_lidos for all
--   using (id_usuario = (select id_usuario from usuario where auth_user_id = auth.uid()))
--   with check (id_usuario = (select id_usuario from usuario where auth_user_id = auth.uid()));
--
-- (Replicar o padrão para lista_desejos, avaliacao, uso_filtro, multa, aluguel.)
-- Regras de aluguel/devolução/multa com transações (INT-1) DEVEM passar por
-- funções SECURITY DEFINER ou API própria, nunca por RLS direto.

-- Fim do schema — Aurora v3.0 para Supabase
