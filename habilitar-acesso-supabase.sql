-- ============================================================================
-- AURORA — Habilitação de Acesso Direto para Front-end no Supabase
-- Copie e cole este script no SQL Editor do Supabase e clique em RUN
-- ============================================================================

-- 1. Criação de tabelas e colunas complementares necessárias pelo Front-end
create table if not exists fila_espera (
  id bigint generated always as identity primary key,
  id_livro bigint not null references livro (id_livro) on delete cascade,
  id_usuario bigint not null references usuario (id_usuario) on delete cascade,
  data_entrada timestamptz not null default now(),
  unique (id_livro, id_usuario)
);

alter table if exists avaliacao add column if not exists comentario text;

-- 2. Desabilita RLS nas tabelas para permitir gravação direta pelo Front-end no GitHub Pages
alter table if exists usuario disable row level security;
alter table if exists livro disable row level security;
alter table if exists exemplar disable row level security;
alter table if exists aluguel disable row level security;
alter table if exists ja_lidos disable row level security;
alter table if exists lista_desejos disable row level security;
alter table if exists avaliacao disable row level security;
alter table if exists uso_filtro disable row level security;
alter table if exists multa disable row level security;
alter table if exists contato_suporte disable row level security;
alter table if exists configuracao disable row level security;
alter table if exists fila_espera disable row level security;

-- 3. Concede permissões para as roles públicas do Supabase (anon e authenticated)
grant usage on schema public to anon, authenticated, postgres;
grant all on all tables in schema public to anon, authenticated, postgres;
grant all on all sequences in schema public to anon, authenticated, postgres;
grant all on all routines in schema public to anon, authenticated, postgres;

-- 4. Inserção dos Usuários Padrão se ainda não existirem
insert into usuario (nome, email, senha_hash, tipo, end_cidade, end_uf) values
  ('Administrador Aurora', 'admin@aurora.local', 'admin123', 'administrador', 'São Paulo', 'SP'),
  ('Bibliotecário Aurora', 'biblio@aurora.local', 'biblio123', 'bibliotecario', 'São Paulo', 'SP'),
  ('Helena Souza', 'aluno@aurora.local', 'aluno123', 'aluno', 'São Paulo', 'SP')
on conflict (email) do nothing;

-- 5. Inserção dos 14 Livros Oficiais se a tabela estiver vazia
insert into livro (titulo, autor, genero, num_paginas, valor_livro, preco_aluguel, faixa_tamanho, capa_url, data_lancamento, avaliacao_media, total_avaliacoes, alugueis_ultimo_ano, eh_infantil)
select * from (values
  ('O Pequeno Príncipe', 'Antoine de Saint-Exupéry', 'Infantil', 96, 35.00, 5.00, 'pequeno'::faixa_tamanho, 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&q=80', '2026-01-15'::date, 4.9, 320, 142, true),
  ('A Metamorfose', 'Franz Kafka', 'Ficção Clássica', 88, 30.00, 4.50, 'pequeno'::faixa_tamanho, 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=400&q=80', '2026-02-10'::date, 4.6, 210, 89, false),
  ('A Revolução dos Bichos', 'George Orwell', 'Fábula Política', 95, 32.00, 5.00, 'pequeno'::faixa_tamanho, 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&q=80', '2026-03-01'::date, 4.8, 410, 178, false),
  ('O Alienista', 'Machado de Assis', 'Clássico Brasileiro', 120, 28.00, 4.00, 'medio_pequeno'::faixa_tamanho, 'https://images.unsplash.com/photo-1495640388908-05fa85288e61?w=400&q=80', '2026-01-20'::date, 4.7, 185, 76, false),
  ('O Velho e o Mar', 'Ernest Hemingway', 'Drama', 128, 38.00, 6.00, 'medio_pequeno'::faixa_tamanho, 'https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=400&q=80', '2026-04-05'::date, 4.7, 195, 92, false),
  ('Fahrenheit 451', 'Ray Bradbury', 'Ficção Científica', 144, 42.00, 6.50, 'medio_pequeno'::faixa_tamanho, 'https://images.unsplash.com/photo-1507842229451-77b5a83a42eb?w=400&q=80', '2026-05-12'::date, 4.8, 340, 160, false),
  ('Noites Brancas', 'Fiódor Dostoiévski', 'Romance', 112, 34.00, 5.00, 'medio_pequeno'::faixa_tamanho, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&q=80', '2026-05-22'::date, 4.9, 160, 58, false),
  ('Dom Casmurro', 'Machado de Assis', 'Romance Clássico', 208, 45.00, 7.00, 'medio_padrao'::faixa_tamanho, 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=400&q=80', '2026-02-18'::date, 4.8, 520, 230, false),
  ('1984', 'George Orwell', 'Ficção Científica', 240, 48.00, 7.50, 'medio_padrao'::faixa_tamanho, 'https://images.unsplash.com/photo-1531988042231-d39a9cc12a9a?w=400&q=80', '2026-04-10'::date, 4.9, 610, 310, false),
  ('O Carteiro e o Poeta', 'Antonio Skármeta', 'Romance', 168, 36.00, 5.50, 'medio_padrao'::faixa_tamanho, 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=400&q=80', '2026-03-25'::date, 4.5, 98, 45, false),
  ('Vidas Secas', 'Graciliano Ramos', 'Clássico Brasileiro', 176, 39.00, 6.00, 'medio_padrao'::faixa_tamanho, 'https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=400&q=80', '2026-01-30'::date, 4.8, 280, 115, false),
  ('Cem Anos de Solidão', 'Gabriel García Márquez', 'Realismo Mágico', 448, 65.00, 9.00, 'grande'::faixa_tamanho, 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&q=80', '2026-03-15'::date, 4.9, 740, 290, false),
  ('Crime e Castigo', 'Fiódor Dostoiévski', 'Ficção Clássica', 592, 72.00, 10.00, 'grande'::faixa_tamanho, 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&q=80', '2026-02-05'::date, 4.8, 430, 180, false),
  ('Grande Sertão: Veredas', 'João Guimarães Rosa', 'Clássico Brasileiro', 624, 85.00, 11.50, 'grande'::faixa_tamanho, 'https://images.unsplash.com/photo-1495640388908-05fa85288e61?w=400&q=80', '2026-04-01'::date, 4.7, 310, 125, false)
) as v(titulo, autor, genero, num_paginas, valor_livro, preco_aluguel, faixa_tamanho, capa_url, data_lancamento, avaliacao_media, total_avaliacoes, alugueis_ultimo_ano, eh_infantil)
where not exists (select 1 from livro limit 1);

-- 6. Inserção dos Exemplares (3 cópias por livro = 42 exemplares disponíveis) se não existirem
insert into exemplar (id_livro, status)
select id_livro, 'disponivel'::exemplar_status
from livro
cross join (values (1), (2), (3)) as copies(n)
where not exists (select 1 from exemplar limit 1);

-- 7. Chaves Dinâmicas Iniciais de Administrador e Bibliotecário
insert into configuracao (chave, valor) values
  ('codigo_admin', 'AURORA-ADM-7842-X9'),
  ('codigo_biblio', 'AURORA-BIB-3914-K2')
on conflict (chave) do nothing;
