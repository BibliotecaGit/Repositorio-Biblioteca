-- Schema para o Sistema de Biblioteca

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario VARCHAR(36) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    turma VARCHAR(50) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'aluno', -- 'aluno', 'bibliotecario', 'administrador'
    bloqueado_ate DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS livro (
    id_livro VARCHAR(36) PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(255) NOT NULL,
    genero VARCHAR(100) NOT NULL,
    tamanho VARCHAR(20) NOT NULL, -- 'pequeno', 'grande'
    num_paginas INTEGER NOT NULL,
    valor_livro DECIMAL(10,2) NOT NULL,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS exemplar (
    id_exemplar VARCHAR(36) PRIMARY KEY,
    id_livro VARCHAR(36) NOT NULL,
    codigo_barras VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'disponivel', -- 'disponivel', 'alugado', 'reservado', 'danificado', 'indisponivel'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_livro) REFERENCES livro(id_livro)
);

CREATE TABLE IF NOT EXISTS aluguel (
    id_aluguel VARCHAR(36) PRIMARY KEY,
    id_usuario VARCHAR(36) NOT NULL,
    id_exemplar VARCHAR(36) NOT NULL,
    data_aluguel DATE NOT NULL,
    data_devolucao_prevista DATE NOT NULL,
    data_devolucao_real DATE,
    data_extensao DATE,
    extensao_contador INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ativo', -- 'ativo', 'devolvido', 'atrasado', 'cancelado', 'extensao'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_exemplar) REFERENCES exemplar(id_exemplar)
);

CREATE TABLE IF NOT EXISTS fila_espera (
    id_fila VARCHAR(36) PRIMARY KEY,
    id_livro VARCHAR(36) NOT NULL,
    id_usuario VARCHAR(36) NOT NULL,
    posicao INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'aguardando', -- 'aguardando', 'notificado', 'confirmado', 'expirado', 'desistiu'
    data_entrada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_notificacao TIMESTAMP,
    data_limite_resposta DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_livro) REFERENCES livro(id_livro),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    UNIQUE(id_livro, id_usuario)
);

CREATE TABLE IF NOT EXISTS multa (
    id_multa VARCHAR(36) PRIMARY KEY,
    id_aluguel VARCHAR(36) NOT NULL,
    id_usuario VARCHAR(36) NOT NULL,
    tipo VARCHAR(20) NOT NULL, -- 'atraso', 'dano'
    tipo_dano VARCHAR(50),
    valor DECIMAL(10,2) NOT NULL,
    dias_atraso INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'pendente', -- 'pendente', 'pago'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_aluguel) REFERENCES aluguel(id_aluguel),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);
