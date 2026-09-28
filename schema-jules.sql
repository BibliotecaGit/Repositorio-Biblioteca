-- ============================================================================
-- AURORA — Sistema de Biblioteca com Aluguel de Livros
-- Esquema SQL Completo para Supabase (PostgreSQL 15+)
-- Adaptado da Espec v3.0 (SPEC.md) e Backlog Detalhado (TASKS.md)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 0. LIMPEZA / DROPS (para re-execução segura no SQL Editor do Supabase)
-- ----------------------------------------------------------------------------
drop view if exists vw_destaques cascade;
drop view if exists vw_novidades cascade;

drop table if exists contato_suporte cascade;
drop table if exists configuracao cascade;
drop table if exists multa cascade;
drop table if exists uso_filtro cascade;
drop table if exists avaliacao cascade;
drop table if exists lista_desejos cascade;
drop table if exists ja_lidos cascade;
drop table if exists aluguel cascade;
drop table if exists exemplar cascade;
drop table if exists livro cascade;
drop table if exists usuario cascade;

drop type if exists tipo_filtro cascade;
drop type if exists multa_status cascade;
drop type if exists multa_tipo cascade;
drop type if exists aluguel_status cascade;
drop type if exists exemplar_status cascade;
drop type if exists faixa_tamanho cascade;
drop type if exists usuario_tema cascade;
drop type if exists usuario_idioma cascade;
drop type if exists usuario_tipo cascade;

-- ----------------------------------------------------------------------------
-- 1. EXTENSÕES
-- ----------------------------------------------------------------------------
create extension if not exists "pgcrypto";

-- ----------------------------------------------------------------------------
-- 2. ENUMS (§6.2 da Spec v3.0)
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
-- 3. TABELAS (§6.3 da Spec v3.0)
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

-- CONTATO_SUPORTE (RF-029.3 — comunicação de suporte) ----------------------
create table contato_suporte (
  id            bigint generated always as identity primary key,
  id_usuario    bigint references usuario (id_usuario) on delete set null,
  email         varchar(190) not null,
  assunto       varchar(190) not null,
  mensagem      text not null,
  respondido    boolean not null default false,
  created_at    timestamptz not null default now()
);

-- CONFIGURACAO (texto institucional editável, CA-018.1) ----------------------
create table configuracao (
  chave       varchar(80) primary key,
  valor       text not null,
  updated_at  timestamptz not null default now()
);

-- ----------------------------------------------------------------------------
-- 4. ÍNDICES DE PERFORMANCE (§6.4)
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

-- ----------------------------------------------------------------------------
-- 5. FUNÇÕES E TRIGGERS
-- ----------------------------------------------------------------------------

-- 5.1 Touch updated_at automático ---------------------------------------------
create or replace function fn_touch_updated_at() returns trigger as $$
begin
  new.updated_at := now();
  return new;
end;
$$ language plpgsql;

create trigger trg_usuario_updated    before update on usuario    for each row execute function fn_touch_updated_at();
create trigger trg_livro_updated      before update on livro      for each row execute function fn_touch_updated_at();
create trigger trg_exemplar_updated   before update on exemplar   for each row execute function fn_touch_updated_at();
create trigger trg_aluguel_updated    before update on aluguel    for each row execute function fn_touch_updated_at();
create trigger trg_uso_filtro_updated before update on uso_filtro for each row execute function fn_touch_updated_at();
create trigger trg_config_updated     before update on configuracao for each row execute function fn_touch_updated_at();

-- 5.2 Faixa de tamanho e flag infantil automática (RN-001 / CA-004.2 / CA-008.3)
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

-- 5.3 Recálculo de avaliacao_media e total_avaliacoes (RN-009 / RNF-004) ------
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
-- 6. VIEWS DE NEGÓCIO
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
   and l.alugueis_ultimo_ano >= coalesce(p.p80, 0)
 order by l.alugueis_ultimo_ano desc;

-- ----------------------------------------------------------------------------
-- 7. FUNÇÕES AUXILIARES DE NEGÓCIO
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
-- 8. SEEDS MÍNIMOS E CONFIGURAÇÃO INICIAL
-- ----------------------------------------------------------------------------
insert into configuracao (chave, valor) values
  ('descricao_institucional',
   'A Aurora é uma biblioteca comunitária de aluguel de livros, com a missão de incentivar a leitura. Nosso acervo possui 3 cópias por título.')
on conflict (chave) do update set valor = excluded.valor;

-- ----------------------------------------------------------------------------
-- 9. ROW LEVEL SECURITY (Supabase RLS)
-- ----------------------------------------------------------------------------
-- Para ativar RLS no Supabase para coleções e ações do usuário:
-- alter table ja_lidos enable row level security;
-- alter table lista_desejos enable row level security;
-- alter table avaliacao enable row level security;
-- alter table uso_filtro enable row level security;
-- alter table aluguel enable row level security;
-- alter table multa enable row level security;
