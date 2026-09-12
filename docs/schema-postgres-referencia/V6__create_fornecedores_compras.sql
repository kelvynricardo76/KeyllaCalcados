-- ═══════════════════════════════════════════════════════════════
-- V6 — Fornecedores e Entradas de Mercadoria
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE fornecedores (
    id              BIGSERIAL PRIMARY KEY,
    razao_social    VARCHAR(120) NOT NULL,
    cnpj            VARCHAR(18),
    telefone        VARCHAR(20),
    email           VARCHAR(120),
    endereco        VARCHAR(200),
    contato         VARCHAR(80),                            -- nome do representante
    observacoes     TEXT,
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_fornecedor_cnpj ON fornecedores(cnpj) WHERE cnpj IS NOT NULL;

CREATE TYPE status_compra AS ENUM ('PENDENTE', 'RECEBIDA', 'PARCIAL', 'CANCELADA');

CREATE TABLE compras (
    id              BIGSERIAL PRIMARY KEY,
    fornecedor_id   BIGINT REFERENCES fornecedores(id) ON DELETE SET NULL,
    loja_id         BIGINT NOT NULL REFERENCES lojas(id),
    usuario_id      BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    data_compra     DATE NOT NULL DEFAULT CURRENT_DATE,
    data_entrega    DATE,
    numero_nf       VARCHAR(60),                            -- número da nota fiscal de entrada
    status          status_compra NOT NULL DEFAULT 'PENDENTE',
    valor_total     NUMERIC(12,2) NOT NULL DEFAULT 0,
    observacoes     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_compra_fornecedor ON compras(fornecedor_id);
CREATE INDEX idx_compra_status     ON compras(status);

CREATE TABLE itens_compra (
    id                      BIGSERIAL PRIMARY KEY,
    compra_id               BIGINT NOT NULL REFERENCES compras(id) ON DELETE CASCADE,
    variacao_id             BIGINT NOT NULL REFERENCES variacoes_produto(id),
    quantidade              INT NOT NULL CHECK (quantidade > 0),
    quantidade_recebida     INT NOT NULL DEFAULT 0,
    preco_custo_unitario    NUMERIC(12,2) NOT NULL,
    subtotal                NUMERIC(12,2) NOT NULL
);

CREATE INDEX idx_item_compra ON itens_compra(compra_id);
