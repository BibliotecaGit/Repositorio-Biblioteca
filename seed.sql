-- ============================================================================
-- AURORA — Sistema de Biblioteca com Aluguel de Livros
-- Script de SEED Oficial para Supabase (PostgreSQL 15+)
-- ============================================================================

-- 1. USUÁRIOS INICIAIS (§6.7 da Spec v3.0)
-- Senhas padrão:
--   admin@aurora.local   -> admin123
--   biblio@aurora.local  -> biblio123
--   aluno@aurora.local   -> aluno123
-- (Hashes BCrypt padrão cost 10 gerados pelo Spring Security / pgcrypto)

insert into usuario (nome, email, senha_hash, tipo, idioma, tema, cidade, uf)
values
  ('Admin Aurora', 'admin@aurora.local', crypt('admin123', gen_salt('bf', 10)), 'administrador', 'pt-BR', 'claro', 'São Paulo', 'SP'),
  ('Bibliotecário Aurora', 'biblio@aurora.local', crypt('biblio123', gen_salt('bf', 10)), 'bibliotecario', 'pt-BR', 'claro', 'São Paulo', 'SP'),
  ('Helena Souza', 'aluno@aurora.local', crypt('aluno123', gen_salt('bf', 10)), 'aluno', 'pt-BR', 'claro', 'São Paulo', 'SP')
on conflict (email) do nothing;

-- 2. ACERVO COMPLETO (Cobring todas as 4 faixas de tamanho da RN-001)
-- Faixas automáticas via trigger:
--   pequeno:        <= 100 págs
--   medio_pequeno:  101 a 150 págs
--   medio_padrao:   151 a 250 págs
--   grande:         > 250 págs

insert into livro (titulo, autor, genero, num_paginas, valor_livro, preco_aluguel, capa_url, data_lancamento, avaliacao_media, total_avaliacoes, alugueis_ultimo_ano, eh_infantil, faixa_tamanho)
values
  -- FAIXA 1: PEQUENO (<= 100 págs)
  ('O Pequeno Príncipe', 'Antoine de Saint-Exupéry', 'Infantil', 96, 35.00, 5.00,
   'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&q=80', current_date - interval '2 months', 4.9, 128, 45, true, 'pequeno'),

  ('A Metamorfose', 'Franz Kafka', 'Ficção Clássica', 88, 30.00, 4.50,
   'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=400&q=80', current_date - interval '8 months', 4.6, 95, 32, false, 'pequeno'),

  ('A Revolução dos Bichos', 'George Orwell', 'Fábula Política', 95, 32.00, 5.00,
   'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&q=80', current_date - interval '3 months', 4.8, 210, 60, false, 'pequeno'),

  -- FAIXA 2: MÉDIO-PEQUENO (101 a 150 págs)
  ('O Alienista', 'Machado de Assis', 'Clássico Brasileiro', 120, 28.00, 4.00,
   'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=400&q=80', current_date - interval '1 year', 4.7, 85, 29, false, 'medio_pequeno'),

  ('O Velho e o Mar', 'Ernest Hemingway', 'Drama', 128, 38.00, 6.00,
   'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=400&q=80', current_date - interval '5 months', 4.8, 140, 52, false, 'medio_pequeno'),

  ('Fahrenheit 451', 'Ray Bradbury', 'Ficção Científica', 144, 42.00, 6.50,
   'https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=400&q=80', current_date - interval '1 month', 4.7, 115, 48, false, 'medio_pequeno'),

  ('Noites Brancas', 'Fiódor Dostoiévski', 'Romance', 112, 34.00, 5.00,
   'https://images.unsplash.com/photo-1532012164546-f432f2e3777f?w=400&q=80', current_date - interval '4 months', 4.9, 160, 58, false, 'medio_pequeno'),

  -- FAIXA 3: MÉDIO-PADRÃO (151 a 250 págs)
  ('Dom Casmurro', 'Machado de Assis', 'Romance Clássico', 208, 45.00, 7.00,
   'https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=400&q=80', current_date - interval '2 years', 4.7, 310, 85, false, 'medio_padrao'),

  ('O Hobbit', 'J.R.R. Tolkien', 'Fantasia', 240, 55.00, 8.50,
   'https://images.unsplash.com/photo-1629992101753-56d196c8aabb?w=400&q=80', current_date - interval '3 months', 4.9, 420, 110, false, 'medio_padrao'),

  ('Admirável Mundo Novo', 'Aldous Huxley', 'Ficção Científica', 224, 48.00, 7.50,
   'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=400&q=80', current_date - interval '7 months', 4.6, 175, 42, false, 'medio_padrao'),

  ('O Meu Pé de Laranja Lima', 'José Mauro de Vasconcelos', 'Infantil', 192, 40.00, 6.00,
   'https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=400&q=80', current_date - interval '2 months', 4.8, 190, 65, true, 'medio_padrao'),

  -- FAIXA 4: GRANDE (> 250 págs)
  ('1984', 'George Orwell', 'Ficção Científica', 328, 55.00, 9.00,
   'https://images.unsplash.com/photo-1541963463532-d68292c34b19?w=400&q=80', current_date - interval '1 year', 4.9, 520, 140, false, 'grande'),

  ('Cem Anos de Solidão', 'Gabriel García Márquez', 'Realismo Mágico', 448, 65.00, 10.00,
   'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&q=80', current_date - interval '4 months', 4.9, 380, 95, false, 'grande'),

  ('Orgulho e Preconceito', 'Jane Austen', 'Romance', 360, 50.00, 8.00,
   'https://images.unsplash.com/photo-1463320726281-696a485928c7?w=400&q=80', current_date - interval '6 months', 4.8, 290, 78, false, 'grande')
on conflict do nothing;

-- 3. CRIAÇÃO DE 3 EXEMPLARES FÍSICOS POR TÍTULO (RN-003 da Spec v3.0)
-- Garante exatamente 3 cópias físicas com status "disponivel" para cada livro cadastrado.

do $$
declare
  r_livro record;
  i integer;
  v_codigo varchar(64);
begin
  for r_livro in (select id_livro from livro) loop
    -- Só cria os exemplares se o livro ainda não tiver 3 exemplares
    if (select count(*) from exemplar where id_livro = r_livro.id_livro) < 3 then
      for i in 1..3 loop
        v_codigo := 'AUR-' || lpad(r_livro.id_livro::text, 4, '0') || '-EX' || i;
        insert into exemplar (id_livro, codigo_barras, status)
        values (r_livro.id_livro, v_codigo, 'disponivel')
        on conflict (codigo_barras) do nothing;
      end loop;
    end if;
  end loop;
end $$;

-- 4. CONFIGURAÇÕES ADICIONAIS
insert into configuracao (chave, valor) values
  ('nome_sistema', 'Aurora'),
  ('max_alugueis_simultaneos', '3'),
  ('taxa_multa_dia', '3.00'),
  ('max_extensoes', '2')
on conflict (chave) do update set valor = excluded.valor;
