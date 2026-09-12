-- ═══════════════════════════════════════════════════════════════
-- V2 — Catálogo de Produtos (Categorias, Marcas, Produtos, Grade)
-- ═══════════════════════════════════════════════════════════════

-- Lojas (multi-loja pronto desde o início)
CREATE TABLE lojas (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    cnpj        VARCHAR(18),
    telefone    VARCHAR(20),
    endereco    VARCHAR(200),
    logo_url    VARCHAR(500),
    ativo       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Categorias em árvore (categoria_pai_id = NULL → categoria raiz)
CREATE TABLE categorias (
    id                  BIGSERIAL PRIMARY KEY,
    nome                VARCHAR(80) NOT NULL,
    categoria_pai_id    BIGINT REFERENCES categorias(id) ON DELETE SET NULL,
    descricao           TEXT,
    ordem               INT DEFAULT 0,
    ativo               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índice para queries de árvore (filhos de uma categoria)
CREATE INDEX idx_categoria_pai ON categorias(categoria_pai_id);

-- Marcas
CREATE TABLE marcas (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(80) NOT NULL UNIQUE,
    descricao   TEXT,
    logo_url    VARCHAR(500),
    ativo       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Produto base
CREATE TABLE produtos (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(200) NOT NULL,
    descricao       TEXT,
    marca_id        BIGINT REFERENCES marcas(id) ON DELETE SET NULL,
    categoria_id    BIGINT REFERENCES categorias(id) ON DELETE SET NULL,
    codigo_barras   VARCHAR(50),
    sku             VARCHAR(60) UNIQUE,
    preco_custo     NUMERIC(12,2) NOT NULL DEFAULT 0,
    preco_venda     NUMERIC(12,2) NOT NULL DEFAULT 0,
    -- margem calculada: ((venda - custo) / venda) * 100 — calculada em runtime, não armazenada
    tem_grade       BOOLEAN NOT NULL DEFAULT FALSE,      -- FALSE = produto simples (cinto, boné)
    foto_principal_url VARCHAR(500),
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_produto_categoria ON produtos(categoria_id);
CREATE INDEX idx_produto_marca ON produtos(marca_id);
CREATE INDEX idx_produto_sku ON produtos(sku);
CREATE INDEX idx_produto_codbar ON produtos(codigo_barras);

-- Galeria de fotos (produto base + variação específica)
CREATE TABLE fotos_produto (
    id              BIGSERIAL PRIMARY KEY,
    produto_id      BIGINT NOT NULL REFERENCES produtos(id) ON DELETE CASCADE,
    variacao_id     BIGINT,                             -- FK adicionada em V3
    url             VARCHAR(500) NOT NULL,
    thumbnail_url   VARCHAR(500),
    ordem           INT DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Tamanhos/numerações
CREATE TYPE tipo_tamanho AS ENUM ('NUMERO', 'LETRA', 'UNICO');

CREATE TABLE tamanhos (
    id      BIGSERIAL PRIMARY KEY,
    valor   VARCHAR(10) NOT NULL UNIQUE,               -- '31', '32', 'P', 'M', 'ÚNICO'
    tipo    tipo_tamanho NOT NULL DEFAULT 'NUMERO',
    ordem   INT DEFAULT 0                              -- para ordenação correta (31 < 32 < 33...)
);

-- Cores
CREATE TABLE cores (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(50) NOT NULL UNIQUE,
    hex_code    VARCHAR(7)                             -- '#FF5733'
);
