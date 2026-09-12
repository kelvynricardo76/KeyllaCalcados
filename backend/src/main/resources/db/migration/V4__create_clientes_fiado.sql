-- ═══════════════════════════════════════════════════════════════
-- V4 — Clientes e Controle de Fiado
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE clientes (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(120) NOT NULL,
    cpf             VARCHAR(14),                            -- formato '000.000.000-00'
    cnpj            VARCHAR(18),
    telefone        VARCHAR(20),
    email           VARCHAR(120),
    data_nascimento DATE,
    -- Endereço
    cep             VARCHAR(9),
    logradouro      VARCHAR(120),
    numero          VARCHAR(10),
    complemento     VARCHAR(60),
    bairro          VARCHAR(80),
    cidade          VARCHAR(80),
    uf              CHAR(2),
    -- Crédito/Fiado
    limite_fiado    NUMERIC(12,2),                          -- NULL = sem limite definido
    observacoes     TEXT,
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_cliente_doc CHECK (cpf IS NOT NULL OR cnpj IS NOT NULL OR telefone IS NOT NULL)
);

CREATE UNIQUE INDEX idx_cliente_cpf  ON clientes(cpf)  WHERE cpf IS NOT NULL;
CREATE UNIQUE INDEX idx_cliente_cnpj ON clientes(cnpj) WHERE cnpj IS NOT NULL;
CREATE INDEX idx_cliente_nome        ON clientes(nome);
CREATE INDEX idx_cliente_telefone    ON clientes(telefone);

-- ─── Fiado ────────────────────────────────────────────────────
CREATE TYPE status_fiado AS ENUM ('ABERTO', 'PARCIAL', 'PAGO', 'VENCIDO');

CREATE TABLE fiados (
    id                  BIGSERIAL PRIMARY KEY,
    cliente_id          BIGINT NOT NULL REFERENCES clientes(id),
    loja_id             BIGINT NOT NULL REFERENCES lojas(id),
    venda_id            BIGINT,                             -- FK adicionada em V5 (vendas)
    valor_total         NUMERIC(12,2) NOT NULL,
    valor_pago          NUMERIC(12,2) NOT NULL DEFAULT 0,
    valor_restante      NUMERIC(12,2) NOT NULL,             -- = valor_total - valor_pago
    data_lancamento     DATE NOT NULL DEFAULT CURRENT_DATE,
    data_vencimento     DATE NOT NULL,
    status              status_fiado NOT NULL DEFAULT 'ABERTO',
    observacoes         TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fiado_cliente    ON fiados(cliente_id);
CREATE INDEX idx_fiado_status     ON fiados(status);
CREATE INDEX idx_fiado_vencimento ON fiados(data_vencimento);

-- Pagamentos de fiado (parcial ou total)
CREATE TABLE pagamentos_fiado (
    id              BIGSERIAL PRIMARY KEY,
    fiado_id        BIGINT NOT NULL REFERENCES fiados(id),
    usuario_id      BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    valor           NUMERIC(12,2) NOT NULL,
    forma_pagamento VARCHAR(30) NOT NULL,                   -- DINHEIRO, PIX, CARTAO, etc.
    data_pagamento  DATE NOT NULL DEFAULT CURRENT_DATE,
    observacoes     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pgfiado_fiado ON pagamentos_fiado(fiado_id);
