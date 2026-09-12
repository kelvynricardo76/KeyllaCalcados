-- ═══════════════════════════════════════════════════════════════
-- V5 — Caixa, Sessões e Vendas (PDV)
-- ═══════════════════════════════════════════════════════════════

-- ─── Caixa e Sessões ─────────────────────────────────────────
CREATE TABLE caixas (
    id          BIGSERIAL PRIMARY KEY,
    loja_id     BIGINT NOT NULL REFERENCES lojas(id),
    nome        VARCHAR(60) NOT NULL,                       -- 'Caixa 1', 'Caixa 2'
    ativo       BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TYPE status_sessao AS ENUM ('ABERTA', 'FECHADA');

CREATE TABLE sessoes_caixa (
    id                              BIGSERIAL PRIMARY KEY,
    caixa_id                        BIGINT NOT NULL REFERENCES caixas(id),
    usuario_id                      BIGINT NOT NULL REFERENCES usuarios(id),
    data_abertura                   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    valor_abertura                  NUMERIC(12,2) NOT NULL DEFAULT 0,
    data_fechamento                 TIMESTAMPTZ,
    valor_fechamento_informado      NUMERIC(12,2),          -- o que o operador contou
    valor_fechamento_calculado      NUMERIC(12,2),          -- o que o sistema calculou
    diferenca                       NUMERIC(12,2),          -- calculado - informado
    status                          status_sessao NOT NULL DEFAULT 'ABERTA',
    observacoes                     TEXT
);

-- Índice único parcial: garante no máximo 1 sessão ABERTA por caixa,
-- sem limitar o histórico de sessões FECHADAS (um UNIQUE(caixa_id, status)
-- simples impediria uma segunda sessão fechada para o mesmo caixa).
CREATE UNIQUE INDEX idx_uma_sessao_aberta_por_caixa
    ON sessoes_caixa(caixa_id)
    WHERE status = 'ABERTA';

-- Movimentações avulsas de caixa (suprimento, sangria, despesas)
CREATE TYPE tipo_mov_caixa AS ENUM ('SUPRIMENTO', 'SANGRIA', 'DESPESA', 'RECEITA_AVULSA');

CREATE TABLE movimentacoes_caixa (
    id              BIGSERIAL PRIMARY KEY,
    sessao_id       BIGINT NOT NULL REFERENCES sessoes_caixa(id),
    usuario_id      BIGINT NOT NULL REFERENCES usuarios(id),
    tipo            tipo_mov_caixa NOT NULL,
    valor           NUMERIC(12,2) NOT NULL,
    descricao       TEXT NOT NULL,
    categoria       VARCHAR(60),                            -- 'Troco', 'Fornecedor', 'Limpeza', etc.
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── Vendas ──────────────────────────────────────────────────
CREATE TYPE status_venda AS ENUM ('ABERTA', 'FECHADA', 'CANCELADA');
CREATE TYPE status_orcamento AS ENUM ('PENDENTE', 'CONVERTIDO', 'EXPIRADO');

CREATE TABLE vendas (
    id                      BIGSERIAL PRIMARY KEY,
    sessao_id               BIGINT REFERENCES sessoes_caixa(id),
    cliente_id              BIGINT REFERENCES clientes(id) ON DELETE SET NULL,
    usuario_id              BIGINT NOT NULL REFERENCES usuarios(id),    -- vendedor
    loja_id                 BIGINT NOT NULL REFERENCES lojas(id),
    status                  status_venda NOT NULL DEFAULT 'ABERTA',
    subtotal                NUMERIC(12,2) NOT NULL DEFAULT 0,
    desconto_geral          NUMERIC(12,2) NOT NULL DEFAULT 0,
    valor_total             NUMERIC(12,2) NOT NULL DEFAULT 0,
    troco                   NUMERIC(12,2) DEFAULT 0,
    observacoes             TEXT,
    -- Orçamento
    is_orcamento            BOOLEAN NOT NULL DEFAULT FALSE,
    orcamento_validade      DATE,
    orcamento_status        status_orcamento,
    -- Cancelamento
    cancelado_em            TIMESTAMPTZ,
    cancelado_por           BIGINT REFERENCES usuarios(id),
    motivo_cancelamento     TEXT,
    -- Referência para troca/devolução
    venda_origem_id         BIGINT REFERENCES vendas(id),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_venda_sessao   ON vendas(sessao_id);
CREATE INDEX idx_venda_cliente  ON vendas(cliente_id);
CREATE INDEX idx_venda_usuario  ON vendas(usuario_id);
CREATE INDEX idx_venda_created  ON vendas(created_at DESC);
CREATE INDEX idx_venda_status   ON vendas(status);
CREATE INDEX idx_venda_orcamento ON vendas(is_orcamento, orcamento_status) WHERE is_orcamento = TRUE;

-- FK de fiados para vendas (agora que vendas existe)
ALTER TABLE fiados ADD CONSTRAINT fk_fiado_venda
    FOREIGN KEY (venda_id) REFERENCES vendas(id) ON DELETE SET NULL;

-- Itens da venda
-- Armazenamos snapshot do nome/SKU porque produto pode mudar depois da venda
CREATE TABLE itens_venda (
    id                      BIGSERIAL PRIMARY KEY,
    venda_id                BIGINT NOT NULL REFERENCES vendas(id) ON DELETE CASCADE,
    variacao_id             BIGINT REFERENCES variacoes_produto(id) ON DELETE SET NULL,
    produto_id              BIGINT REFERENCES produtos(id) ON DELETE SET NULL,
    nome_produto_snapshot   VARCHAR(200) NOT NULL,
    sku_snapshot            VARCHAR(80),
    tamanho_snapshot        VARCHAR(10),
    cor_snapshot            VARCHAR(50),
    quantidade              INT NOT NULL CHECK (quantidade > 0),
    preco_unitario          NUMERIC(12,2) NOT NULL,
    desconto_item           NUMERIC(12,2) NOT NULL DEFAULT 0,
    subtotal                NUMERIC(12,2) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_item_venda ON itens_venda(venda_id);

-- Formas de pagamento da venda (múltiplas por venda)
CREATE TYPE forma_pagamento AS ENUM (
    'DINHEIRO', 'DEBITO', 'CREDITO', 'PIX', 'FIADO', 'TROCA', 'OUTRO'
);

CREATE TABLE pagamentos_venda (
    id              BIGSERIAL PRIMARY KEY,
    venda_id        BIGINT NOT NULL REFERENCES vendas(id) ON DELETE CASCADE,
    forma           forma_pagamento NOT NULL,
    valor           NUMERIC(12,2) NOT NULL,
    parcelas        INT NOT NULL DEFAULT 1,
    referencia      VARCHAR(100),                           -- NSU, código PIX, etc.
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pgvenda_venda ON pagamentos_venda(venda_id);

-- ─── Devoluções / Trocas ─────────────────────────────────────
CREATE TYPE tipo_devolucao AS ENUM ('TROCA', 'DEVOLUCAO');

CREATE TABLE devolucoes (
    id                  BIGSERIAL PRIMARY KEY,
    venda_origem_id     BIGINT NOT NULL REFERENCES vendas(id),
    venda_nova_id       BIGINT REFERENCES vendas(id),       -- nova venda (troca)
    usuario_id          BIGINT REFERENCES usuarios(id),
    tipo                tipo_devolucao NOT NULL,
    motivo              TEXT NOT NULL,
    valor_devolvido     NUMERIC(12,2) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
