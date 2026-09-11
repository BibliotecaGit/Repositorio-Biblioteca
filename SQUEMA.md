-- ============================================================================
-- SISTEMA DE BIBLIOTECA COM ALUGUEL DE LIVROS — Esquema SQL (Supabase/PostgreSQL)
-- Baseado na Especificação Técnica v1.0 (2026-09-03)
-- ============================================================================

create extension if not exists "pgcrypto";

-- TIPOS ENUM
create type tipo_usuario    as enum ('aluno', 'bibliotecario', 'administrador');
create type tamanho_livro   as enum ('pequeno', 'grande');
create type status_exemplar as enum ('disponivel', 'alugado', 'reservado', 'danificado', 'indisponivel');
create type status_aluguel  as enum ('ativo', 'devolvido', 'atrasado', 'cancelado');
create type status_fila     as enum ('aguardando', 'notificado', 'confirmado', 'expirado', 'desistiu');
create type tipo_multa      as enum ('atraso', 'dano');
create type status_multa    as enum ('pendente', 'pago');
create type tipo_dano       as enum ('capa_rasgada', 'paginas_faltantes', 'paginas_rasgadas', 'manchas', 'escrita', 'lombada_danificada', 'outros');
create type canal_notificacao as enum ('email', 'in_app');
create type tipo_notificacao as enum (
  'confirmacao_aluguel', 'chamada_fila', 'prazo_proximo', 'atraso_detectado',
  'entrada_fila_extensao', 'devolucao_obrigatoria', 'multa_gerada', 'confirmacao_devolucao'
);

-- USUARIO (RF-001, RF-003)
create table usuario (
  id_usuario    uuid primary key default gen_random_uuid(),
  nome          text not null,
  email         text not null unique,
  cpf           varchar(11) not null unique,
  turma         text not null,
  senha_hash    text not null,
  tipo          tipo_usuario not null default 'aluno',
  bloqueado_ate date,
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now()
);

-- LIVRO (RF-004, RF-007)
create table livro (
  id_livro      uuid primary key default gen_random_uuid(),
  titulo        text not null,
  autor         text not null,
  genero        text not null,
  tamanho       tamanho_livro not null,
  num_paginas   integer not null check (num_paginas > 0),
  valor_livro   numeric(10,2) not null check (valor_livro >= 0),
  deleted_at    timestamptz,
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now()
);

create index idx_livro_titulo  on livro (titulo);
create index idx_livro_autor   on livro (autor);
create index idx_livro_genero  on livro (genero);

-- EXEMPLAR (RF-005)
create table exemplar (
  id_exemplar   uuid primary key default gen_random_uuid(),
  id_livro      uuid not null references livro(id_livro) on delete restrict,
  codigo_barras varchar(50) unique,
  status        status_exemplar not null default 'disponivel',
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now()
);

create index idx_exemplar_livro   on exemplar (id_livro);
create index idx_exemplar_status  on exemplar (status);

-- HISTÓRICO DE DANOS (CA-017.5)
create table historico_dano (
  id_historico  uuid primary key default gen_random_uuid(),
  id_exemplar   uuid not null references exemplar(id_exemplar) on delete cascade,
  tipo_dano     tipo_dano not null,
  observacao    text,
  created_at    timestamptz not null default now()
);

create index idx_historico_dano_exemplar on historico_dano (id_exemplar);

-- ALUGUEL (RF-010, RF-012, RF-013, RF-014)
create table aluguel (
  id_aluguel               uuid primary key default gen_random_uuid(),
  id_usuario               uuid not null references usuario(id_usuario),
  id_exemplar              uuid not null references exemplar(id_exemplar),
  data_aluguel             date not null default current_date,
  data_devolucao_prevista  date not null,
  data_devolucao_real      date,
  data_extensao            date,
  extensao_contador        integer not null default 0 check (extensao_contador between 0 and 2),
  status                   status_aluguel not null default 'ativo',
  created_at               timestamptz not null default now(),
  updated_at               timestamptz not null default now(),
  check (data_devolucao_real is null or data_devolucao_real >= data_aluguel)
);

create index idx_aluguel_usuario  on aluguel (id_usuario);
create index idx_aluguel_exemplar on aluguel (id_exemplar);
create index idx_aluguel_prevista on aluguel (data_devolucao_prevista) where status = 'ativo';

-- FILA_ESPERA (RF-011, RN-003)
create table fila_espera (
  id_fila               uuid primary key default gen_random_uuid(),
  id_livro              uuid not null references livro(id_livro) on delete restrict,
  id_usuario            uuid not null references usuario(id_usuario),
  posicao               integer not null check (posicao >= 1),
  status                status_fila not null default 'aguardando',
  data_entrada          timestamptz not null default now(),
  data_notificacao      timestamptz,
  data_limite_resposta  date,
  created_at            timestamptz not null default now(),
  updated_at            timestamptz not null default now(),
  unique (id_livro, id_usuario)
);

create index idx_fila_livro_posicao on fila_espera (id_livro, posicao);
create index idx_fila_usuario       on fila_espera (id_usuario);

-- Limite de 5 pessoas por fila (RN-003 / CA-011.5)
create or replace function check_fila_limite()
returns trigger language plpgsql as $$
begin
  if (select count(*) from fila_espera
      where id_livro = new.id_livro
        and status in ('aguardando', 'notificado')) >= 5 then
    raise exception 'Fila cheia: limite de 5 pessoas por livro (RN-003).';
  end if;
  return new;
end $$;

create trigger trg_fila_limite
  before insert on fila_espera
  for each row execute function check_fila_limite();

-- MULTA (RF-016, RF-017)
create table multa (
  id_multa    uuid primary key default gen_random_uuid(),
  id_aluguel  uuid not null references aluguel(id_aluguel),
  id_usuario  uuid not null references usuario(id_usuario),
  tipo        tipo_multa not null,
  tipo_dano   tipo_dano,
  valor       numeric(10,2) not null check (valor >= 0),
  dias_atraso integer,
  status      status_multa not null default 'pendente',
  created_at  timestamptz not null default now(),
  check (tipo <> 'dano' or tipo_dano is not null)
);

create index idx_multa_usuario on multa (id_usuario);
create index idx_multa_status  on multa (status);

-- Multa por dano sempre R$ 20,00 (CA-017.3)
create or replace function fixa_valor_dano()
returns trigger language plpgsql as $$
begin
  if new.tipo = 'dano' then
    new.valor := 20.00;
  end if;
  return new;
end $$;

create trigger trg_multa_dano_valor
  before insert or update on multa
  for each row execute function fixa_valor_dano();

-- NOTIFICACAO (RF-019)
create table notificacao (
  id_notificacao uuid primary key default gen_random_uuid(),
  id_usuario     uuid not null references usuario(id_usuario) on delete cascade,
  tipo           tipo_notificacao not null,
  titulo         text not null,
  mensagem       text not null,
  canal          canal_notificacao not null default 'in_app',
  lida           boolean not null default false,
  id_aluguel     uuid references aluguel(id_aluguel) on delete set null,
  id_fila        uuid references fila_espera(id_fila) on delete set null,
  enviado_em     timestamptz not null default now()
);

create index idx_notificacao_usuario on notificacao (id_usuario, lida);

-- Tamanho automático do livro (CA-004.2 / CA-006.1)
create or replace function define_tamanho_livro()
returns trigger language plpgsql as $$
begin
  if tg_op = 'INSERT'
     or (tg_op = 'UPDATE' and new.num_paginas is distinct from old.num_paginas) then
    new.tamanho := case when new.num_paginas <= 150 then 'pequeno'::tamanho_livro
                        else 'grande'::tamanho_livro end;
  end if;
  return new;
end $$;

create trigger trg_livro_tamanho
  before insert or update of num_paginas on livro
  for each row execute function define_tamanho_livro();

-- updated_at automático
create or replace function set_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at := now();
  return new;
end $$;

create trigger trg_updated_at_usuario   before update on usuario     for each row execute function set_updated_at();
create trigger trg_updated_at_livro     before update on livro       for each row execute function set_updated_at();
create trigger trg_updated_at_exemplar  before update on exemplar    for each row execute function set_updated_at();
create trigger trg_updated_at_aluguel   before update on aluguel     for each row execute function set_updated_at();
create trigger trg_updated_at_fila      before update on fila_espera for each row execute function set_updated_at();

-- View da busca (RF-009): título, autor, gênero, páginas, cópias disponíveis e pessoas na fila
create or replace view vw_livros_disponibilidade as
select
  l.id_livro, l.titulo, l.autor, l.genero, l.num_paginas, l.tamanho, l.valor_livro,
  (select count(*) from exemplar e
    where e.id_livro = l.id_livro and e.status = 'disponivel') as copias_disponiveis,
  (select count(*) from fila_espera f
    where f.id_livro = l.id_livro
      and f.status in ('aguardando', 'notificado')) as pessoas_na_fila
from livro l
where l.deleted_at is null;
