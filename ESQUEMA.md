-- ============================================================================
-- AURORA — Sistema de Biblioteca com Aluguel de Livros
-- DDL MySQL 8.0+ (InnoDB, utf8mb4)
-- Baseado na Especificação Técnica v2.1 (2026-09-24) — Seções 6.3 a 6.5
-- ============================================================================

CREATE DATABASE IF NOT EXISTS aurora
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE aurora;

-- ============================================================================
-- 1. USUARIO (RF-001, RF-028, RF-029, RF-030, RF-031)
-- ============================================================================
CREATE TABLE IF NOT EXISTS USUARIO (
  id_usuario        BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nome              VARCHAR(150)  NOT NULL,
  email             VARCHAR(190)  NOT NULL,
  senha_hash        VARCHAR(255)  NOT NULL              COMMENT 'bcrypt/argon2 (RNF-005)',
  tipo              ENUM('aluno','bibliotecario','administrador')
                    NOT NULL DEFAULT 'aluno',
  foto_url          VARCHAR(500)  NULL,
  idioma            ENUM('pt-BR','en') NOT NULL DEFAULT 'pt-BR',
  tema              ENUM('claro','escuro') NOT NULL DEFAULT 'claro',
  uf                CHAR(2)       NULL                  COMMENT 'regiao (RF-031)',
  cidade            VARCHAR(120)  NULL                  COMMENT 'regiao (RF-031)',
  end_logradouro    VARCHAR(190)  NULL,
  end_numero        VARCHAR(20)   NULL,
  end_complemento   VARCHAR(120)  NULL,
  end_bairro        VARCHAR(120)  NULL,
  end_cidade        VARCHAR(120)  NULL,
  end_uf            CHAR(2)       NULL,
  end_cep           CHAR(8)       NULL                  COMMENT 'apenas digitos; formato validado na aplicacao (RF-028)',
  cartao_token      VARCHAR(255)  NULL                  COMMENT 'tokenizado (RNF-008)',
  cartao_mascarado  VARCHAR(25)   NULL                  COMMENT 'ex.: **** 1234',
  bloqueado_ate     DATE          NULL,
  deleted_at        DATETIME      NULL,
  created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_usuario_email (email)                    -- RF-001/RF-002, §6.4
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 2. LIVRO (RF-004 a RF-008, RN-001, RN-011)
-- ============================================================================
CREATE TABLE IF NOT EXISTS LIVRO (
  id_livro             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  titulo               VARCHAR(255) NOT NULL,
  autor                VARCHAR(190) NOT NULL,
  genero               VARCHAR(80)  NOT NULL,
  faixa_tamanho        ENUM('pequeno','medio_pequeno','medio_padrao','grande')
                       NOT NULL                                  COMMENT 'RN-001; override manual pelo admin (RF-008)',
  num_paginas          INT UNSIGNED NOT NULL CHECK (num_paginas > 0),
  valor_livro          DECIMAL(10,2) NOT NULL CHECK (valor_livro >= 0)   COMMENT 'teto da multa de atraso (RN-004)',
  preco_aluguel        DECIMAL(10,2) NOT NULL CHECK (preco_aluguel >= 0) COMMENT 'RN-008; exibido no hover',
  capa_url             VARCHAR(500) NULL,
  data_lancamento      DATE         NULL                         COMMENT 'alimenta Novidades (RN-011)',
  avaliacao_media      DECIMAL(3,2) NOT NULL DEFAULT 0 CHECK (avaliacao_media BETWEEN 0 AND 5)
                                                            COMMENT 'denormalizado; RN-009, §6.6',
  total_avaliacoes     INT UNSIGNED NOT NULL DEFAULT 0,
  alugueis_ultimo_ano  INT UNSIGNED NOT NULL DEFAULT 0          COMMENT 'alimenta Destaques (RN-011)',
  eh_infantil          TINYINT(1)   NOT NULL DEFAULT 0,
  deleted_at           DATETIME     NULL,
  created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Indices §6.4 — busca e filtros da Prateleira (RF-009, RF-022, RN-011)
CREATE INDEX idx_livro_titulo            ON LIVRO (titulo);
CREATE INDEX idx_livro_autor             ON LIVRO (autor);
CREATE INDEX idx_livro_genero            ON LIVRO (genero);
-- Para escala, substituir os tres indices acima por:
-- CREATE FULLTEXT INDEX ft_livro_busca ON LIVRO (titulo, autor);
CREATE INDEX idx_livro_faixa_tamanho     ON LIVRO (faixa_tamanho);
CREATE INDEX idx_livro_data_lancamento   ON LIVRO (data_lancamento);
CREATE INDEX idx_livro_eh_infantil       ON LIVRO (eh_infantil);
CREATE INDEX idx_livro_avaliacao_media   ON LIVRO (avaliacao_media);
CREATE INDEX idx_livro_alugueis_1ano     ON LIVRO (alugueis_ultimo_ano);

-- ============================================================================
-- 3. EXEMPLAR (RF-005; max. 3 por livro — INT-2 na aplicacao/transacao)
-- ============================================================================
CREATE TABLE IF NOT EXISTS EXEMPLAR (
  id_exemplar     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_livro        BIGINT UNSIGNED NOT NULL,
  codigo_barras   VARCHAR(64) NULL,
  status          ENUM('disponivel','alugado','danificado','manutencao')
                  NOT NULL DEFAULT 'disponivel',
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_exemplar_codigo (codigo_barras),         -- CA-005.2
  KEY idx_exemplar_livro_status (id_livro, status),      -- §6.4: disponibilidade em tempo de aluguel (RF-010)
  CONSTRAINT fk_exemplar_livro FOREIGN KEY (id_livro)
    REFERENCES LIVRO (id_livro) ON DELETE RESTRICT       -- RF-007.1/7.2
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 4. ALUGUEL (RF-010, RF-012, RF-014, RN-001)
-- ============================================================================
CREATE TABLE IF NOT EXISTS ALUGUEL (
  id_aluguel               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_usuario               BIGINT UNSIGNED NOT NULL,
  id_exemplar              BIGINT UNSIGNED NOT NULL,
  data_aluguel             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  data_devolucao_prevista  DATE NOT NULL                         COMMENT 'hoje+15 ou hoje+30 (RN-001)',
  data_devolucao_real      DATE NULL,
  status                   ENUM('ativo','devolvido') NOT NULL DEFAULT 'ativo',
  extensao_contador        TINYINT UNSIGNED NOT NULL DEFAULT 0 CHECK (extensao_contador <= 2),
  preco_cobrado            DECIMAL(10,2) NOT NULL                COMMENT 'preco_aluguel no momento do aluguel',
  created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_aluguel_prevista (data_devolucao_prevista),   -- §6.4: job diario de atrasos (RF-016, RNF-003)
  KEY idx_aluguel_usuario_status (id_usuario, status),  -- §6.4: elegibilidade — <3 alugados, multas (RN-006)
  CONSTRAINT fk_aluguel_usuario  FOREIGN KEY (id_usuario)  REFERENCES USUARIO (id_usuario)  ON DELETE RESTRICT,
  CONSTRAINT fk_aluguel_exemplar FOREIGN KEY (id_exemplar) REFERENCES EXEMPLAR (id_exemplar) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 5. FILA_ESPERA (RF-011, RF-012, RF-013, RF-015; RN-003, INT-4)
-- ============================================================================
CREATE TABLE IF NOT EXISTS FILA_ESPERA (
  id_fila                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_livro               BIGINT UNSIGNED NOT NULL,
  id_usuario             BIGINT UNSIGNED NOT NULL,
  posicao                TINYINT UNSIGNED NOT NULL CHECK (posicao BETWEEN 1 AND 10),
  status                 ENUM('aguardando','notificado','confirmado','expirado','desistiu')
                         NOT NULL DEFAULT 'aguardando',
  data_entrada           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  data_notificacao       DATETIME NULL,
  data_limite_resposta   DATE NULL                              COMMENT 'hoje+3 (RF-012.6)',
  created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  -- INT-4: coluna gerada garante um unico registro ativo por usuario/livro (CA-011A.2)
  fila_ativa             TINYINT GENERATED ALWAYS AS
                         (IF(status IN ('aguardando','notificado'), 1, NULL)) STORED,
  UNIQUE KEY uq_fila_ativa_usuario_livro (id_usuario, id_livro, fila_ativa),
  KEY idx_fila_livro_posicao (id_livro, posicao),       -- §6.4: proximo da fila, limite de 10 (RN-003)
  CONSTRAINT fk_fila_livro   FOREIGN KEY (id_livro)   REFERENCES LIVRO (id_livro)     ON DELETE RESTRICT,
  CONSTRAINT fk_fila_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 6. LISTA_PENDENTES (RF-011A, RF-015; RN-003, INT-4)
-- ============================================================================
CREATE TABLE IF NOT EXISTS LISTA_PENDENTES (
  id_pendente     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_livro        BIGINT UNSIGNED NOT NULL,
  id_usuario      BIGINT UNSIGNED NOT NULL,
  status          ENUM('ativo','promovido','cancelado') NOT NULL DEFAULT 'ativo',
  data_entrada    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  data_promocao   DATETIME NULL,
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  -- INT-4: um pendente ativo por usuario/livro
  pendente_ativo  TINYINT GENERATED ALWAYS AS
                  (IF(status = 'ativo', 1, NULL)) STORED,
  UNIQUE KEY uq_pendente_ativo_usuario_livro (id_usuario, id_livro, pendente_ativo),
  KEY idx_pendente_livro_status_data (id_livro, status, data_entrada), -- §6.4: promocao FIFO (RF-011A)
  CONSTRAINT fk_pendente_livro   FOREIGN KEY (id_livro)   REFERENCES LIVRO (id_livro)     ON DELETE RESTRICT,
  CONSTRAINT fk_pendente_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 7. JA_LIDOS (RF-024; CASCADE — INT-6)
-- ============================================================================
CREATE TABLE IF NOT EXISTS JA_LIDOS (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_usuario    BIGINT UNSIGNED NOT NULL,
  id_livro      BIGINT UNSIGNED NOT NULL,
  data_marcacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_lido (id_usuario, id_livro),
  CONSTRAINT fk_lido_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE,
  CONSTRAINT fk_lido_livro   FOREIGN KEY (id_livro)   REFERENCES LIVRO (id_livro)     ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 8. LISTA_DESEJOS (RF-025; CASCADE — INT-6)
-- ============================================================================
CREATE TABLE IF NOT EXISTS LISTA_DESEJOS (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_usuario    BIGINT UNSIGNED NOT NULL,
  id_livro      BIGINT UNSIGNED NOT NULL,
  data_marcacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_desejo (id_usuario, id_livro),
  CONSTRAINT fk_desejo_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE,
  CONSTRAINT fk_desejo_livro   FOREIGN KEY (id_livro)   REFERENCES LIVRO (id_livro)     ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 9. AVALIACAO (RF-026; RN-009; INT-5 valida JA_LIDOS na aplicacao)
-- ============================================================================
CREATE TABLE IF NOT EXISTS AVALIACAO (
  id_avaliacao    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_usuario      BIGINT UNSIGNED NOT NULL,
  id_livro        BIGINT UNSIGNED NOT NULL,
  nota            DECIMAL(2,1) NOT NULL CHECK (nota BETWEEN 0 AND 5),
  data_avaliacao  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_avaliacao (id_usuario, id_livro),
  KEY idx_avaliacao_livro (id_livro),
  CONSTRAINT fk_aval_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE,
  CONSTRAINT fk_aval_livro   FOREIGN KEY (id_livro)   REFERENCES LIVRO (id_livro)     ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 10. USO_FILTRO (RF-022.7, RF-023; RN-010; CASCADE — INT-6)
-- ============================================================================
CREATE TABLE IF NOT EXISTS USO_FILTRO (
  id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_usuario   BIGINT UNSIGNED NOT NULL,
  tipo_filtro  ENUM('tamanho','genero','autor') NOT NULL,
  valor        VARCHAR(120) NOT NULL,
  contagem     INT UNSIGNED NOT NULL DEFAULT 0,
  UNIQUE KEY uq_uso (id_usuario, tipo_filtro, valor),
  CONSTRAINT fk_usofiltro_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 11. MULTA (RF-016, RF-017, RF-018; RN-004, RN-005)
-- ============================================================================
CREATE TABLE IF NOT EXISTS MULTA (
  id_multa        BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_aluguel      BIGINT UNSIGNED NOT NULL,
  id_usuario      BIGINT UNSIGNED NOT NULL,
  tipo            ENUM('atraso','dano') NOT NULL,
  valor           DECIMAL(10,2) NOT NULL CHECK (valor > 0),
  dias_atraso     SMALLINT UNSIGNED NULL,
  status          ENUM('pendente','pago') NOT NULL DEFAULT 'pendente',
  data_pagamento  DATETIME NULL,
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_multa_usuario_status (id_usuario, status),    -- §6.4: bloqueio por multa (RN-006) e tela RF-018
  KEY idx_multa_aluguel (id_aluguel),
  CONSTRAINT fk_multa_aluguel FOREIGN KEY (id_aluguel) REFERENCES ALUGUEL (id_aluguel) ON DELETE RESTRICT,
  CONSTRAINT fk_multa_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- TRIGGERS — denormalizacao de LIVRO.avaliacao_media/total_avaliacoes
-- (§6.6 e RNF-004: recalculo <= 1s; implementado no banco para garantir
--  coerencia independente do caminho de escrita)
-- ============================================================================

DELIMITER $$

CREATE TRIGGER trg_avaliacao_after_insert
AFTER INSERT ON AVALIACAO
FOR EACH ROW
BEGIN
  UPDATE LIVRO
     SET total_avaliacoes = total_avaliacoes + 1,
         avaliacao_media  = ROUND(((avaliacao_media * total_avaliacoes) + NEW.nota)
                                  / (total_avaliacoes + 1), 2)
   WHERE id_livro = NEW.id_livro;
END$$

CREATE TRIGGER trg_avaliacao_after_update
AFTER UPDATE ON AVALIACAO
FOR EACH ROW
BEGIN
  UPDATE LIVRO
     SET avaliacao_media = ROUND(
           ((avaliacao_media * total_avaliacoes) - OLD.nota + NEW.nota)
           / total_avaliacoes, 2)
   WHERE id_livro = NEW.id_livro;
END$$

CREATE TRIGGER trg_avaliacao_after_delete
AFTER DELETE ON AVALIACAO
FOR EACH ROW
BEGIN
  UPDATE LIVRO
     SET total_avaliacoes = GREATEST(total_avaliacoes - 1, 0),
         avaliacao_media  = IF(total_avaliacoes - 1 > 0,
                               ROUND(((avaliacao_media * total_avaliacoes) - OLD.nota)
                                     / (total_avaliacoes - 1), 2),
                               0)
   WHERE id_livro = OLD.id_livro;
END$$

-- Aluguel confirmado incrementa o contador de alugueis do ultimo ano (§6.6).
-- O job mensal (decremento/remocao de registros com mais de 1 ano) fica na
-- camada de aplicacao conforme §6.6.
CREATE TRIGGER trg_aluguel_after_insert
AFTER INSERT ON ALUGUEL
FOR EACH ROW
BEGIN
  UPDATE LIVRO
     SET alugueis_ultimo_ano = alugueis_ultimo_ano + 1
   WHERE id_livro = (SELECT id_livro FROM EXEMPLAR WHERE id_exemplar = NEW.id_exemplar);
END$$

DELIMITER ;

-- ============================================================================
-- SEEDS OBRIGATORIOS (§6.7): 1 administrador, 1 bibliotecario, 1 aluno
-- Senhas abaixo sao placeholders — substituir por hash bcrypt/argon2 real
-- gerado pela aplicacao antes de qualquer ambiente compartilhado.
-- ============================================================================

INSERT INTO USUARIO (nome, email, senha_hash, tipo) VALUES
('Admin Aurora',       'admin@aurora.local',       '$2y$12$TROCAR_HASH_REAL_ADMIN',       'administrador'),
('Bibliotecario Aurora','biblio@aurora.local',     '$2y$12$TROCAR_HASH_REAL_BIBLIO',      'bibliotecario'),
('Aluno Teste',        'aluno@aurora.local',       '$2y$12$TROCAR_HASH_REAL_ALUNO',       'aluno');

-- ============================================================================
-- NOTAS DE INTEGRIDADE (§6.5) — implementadas na camada de aplicacao:
-- INT-1  transacoes com SELECT ... FOR UPDATE em aluguel/fila/devolucao
-- INT-2  maximo de 3 exemplares por livro (trava dentro da transacao)
-- INT-3  fila limitada a 10 posicoes (insercao condicionada a contagem)
-- INT-4  um registro ativo por usuario/livro — parcialmente coberto pelas
--        colunas geradas + UNIQUE em FILA_ESPERA e LISTA_PENDENTES
-- INT-5  avaliacao exige EXISTS em JA_LIDOS (validacao na escrita)
-- INT-6  RESTRICT no nucleo / CASCADE nas colecoes pessoais
-- INT-7  soft delete: todo SELECT do catalogo filtra deleted_at IS NULL
-- ============================================================================
