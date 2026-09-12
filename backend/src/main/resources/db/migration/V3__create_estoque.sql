-- ═══════════════════════════════════════════════════════════════
-- V3 — Variações de Produto (Grade), Estoque e Localização
-- ═══════════════════════════════════════════════════════════════

-- Cada variação = produto + tamanho + cor → SKU único
CREATE TABLE variacoes_produto (
    id                      BIGSERIAL PRIMARY KEY,
    produto_id              BIGINT NOT NULL REFERENCES produtos(id) ON DELETE CASCADE,
    tamanho_id              BIGINT REFERENCES tamanhos(id) ON DELETE SET NULL,
    cor_id                  BIGINT REFERENCES cores(id) ON DELETE SET NULL,
    sku                     VARCHAR(80) NOT NULL UNIQUE,
    codigo_barras           VARCHAR(50),
    preco_custo_override    NUMERIC(12,2),              -- NULL = usa preço do produto base
    preco_venda_override    NUMERIC(12,2),              -- NULL = usa preço do produto base
    ativo                   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(produto_id, tamanho_id, cor_id)
);

CREATE INDEX idx_variacao_produto ON variacoes_produto(produto_id);
CREATE INDEX idx_variacao_codbar  ON variacoes_produto(codigo_barras);
CREATE INDEX idx_variacao_sku     ON variacoes_produto(sku);

-- FK da foto para a variação (adicionada agora que a tabela existe)
ALTER TABLE fotos_produto
    ADD CONSTRAINT fk_foto_variacao
    FOREIGN KEY (variacao_id) REFERENCES variacoes_produto(id) ON DELETE SET NULL;

-- Localização física no depósito/loja
CREATE TABLE localizacoes_estoque (
    id          BIGSERIAL PRIMARY KEY,
    loja_id     BIGINT NOT NULL REFERENCES lojas(id) ON DELETE CASCADE,
    descricao   VARCHAR(100) NOT NULL,                 -- 'Corredor 3 / Prateleira B / Nível 2'
    corredor    VARCHAR(20),
    prateleira  VARCHAR(20),
    nivel       VARCHAR(20),
    ativo       BOOLEAN NOT NULL DEFAULT TRUE
);

-- Saldo de estoque por variação e loja
-- UNIQUE garante que haja apenas 1 linha de saldo por variação/loja
CREATE TABLE estoque (
    id              BIGSERIAL PRIMARY KEY,
    variacao_id     BIGINT NOT NULL REFERENCES variacoes_produto(id) ON DELETE CASCADE,
    loja_id         BIGINT NOT NULL REFERENCES lojas(id) ON DELETE CASCADE,
    localizacao_id  BIGINT REFERENCES localizacoes_estoque(id) ON DELETE SET NULL,
    quantidade      INT NOT NULL DEFAULT 0 CHECK (quantidade >= 0),
    estoque_minimo  INT NOT NULL DEFAULT 1,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(variacao_id, loja_id)
);

CREATE INDEX idx_estoque_variacao ON estoque(variacao_id);
CREATE INDEX idx_estoque_loja     ON estoque(loja_id);

-- Kardex: histórico completo de toda movimentação de estoque
CREATE TYPE tipo_mov_estoque AS ENUM (
    'ENTRADA',          -- recebimento de compra
    'SAIDA',            -- venda
    'AJUSTE_POSITIVO',  -- ajuste manual (inventário, acerto)
    'AJUSTE_NEGATIVO',  -- perda, avaria
    'TRANSFERENCIA_SAIDA',
    'TRANSFERENCIA_ENTRADA',
    'DEVOLUCAO'         -- devolução de venda
);

CREATE TABLE movimentacoes_estoque (
    id                      BIGSERIAL PRIMARY KEY,
    variacao_id             BIGINT NOT NULL REFERENCES variacoes_produto(id),
    loja_id                 BIGINT NOT NULL REFERENCES lojas(id),
    tipo                    tipo_mov_estoque NOT NULL,
    quantidade              INT NOT NULL,              -- sempre positivo; tipo indica direção
    quantidade_anterior     INT NOT NULL,
    quantidade_posterior    INT NOT NULL,
    referencia_tipo         VARCHAR(30),               -- 'VENDA', 'COMPRA', 'AJUSTE', 'TRANSFERENCIA'
    referencia_id           BIGINT,                    -- ID da venda/compra/etc.
    motivo                  TEXT,
    usuario_id              BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_movest_variacao  ON movimentacoes_estoque(variacao_id);
CREATE INDEX idx_movest_created   ON movimentacoes_estoque(created_at DESC);
CREATE INDEX idx_movest_referencia ON movimentacoes_estoque(referencia_tipo, referencia_id);
