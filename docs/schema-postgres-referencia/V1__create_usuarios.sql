-- ═══════════════════════════════════════════════════════════════
-- V1 — Usuários, Perfis e Permissões
-- ═══════════════════════════════════════════════════════════════

CREATE TYPE perfil_usuario AS ENUM ('ADMIN', 'GERENTE', 'CAIXA', 'ESTOQUISTA');

CREATE TABLE usuarios (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(120) NOT NULL,
    email           VARCHAR(120) NOT NULL UNIQUE,
    senha_hash      VARCHAR(255) NOT NULL,
    pin_hash        VARCHAR(255),                          -- PIN de 4-6 dígitos para troca rápida no caixa
    perfil          perfil_usuario NOT NULL DEFAULT 'CAIXA',
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Permissões granulares sobrescrevem o padrão do perfil
CREATE TABLE user_permissions (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    permissao   VARCHAR(80) NOT NULL,                      -- Ex: 'VER_PRECO_CUSTO', 'DAR_DESCONTO'
    permitido   BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(usuario_id, permissao)
);

-- Log de auditoria central (todas as ações sensíveis)
CREATE TABLE audit_log (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    entidade        VARCHAR(60) NOT NULL,
    entidade_id     BIGINT,
    acao            VARCHAR(40) NOT NULL,                  -- CRIAR, EDITAR, EXCLUIR, CANCELAR, LOGIN, etc.
    dados_antes     JSONB,
    dados_depois    JSONB,
    ip_address      VARCHAR(45),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_entidade ON audit_log(entidade, entidade_id);
CREATE INDEX idx_audit_usuario  ON audit_log(usuario_id);
CREATE INDEX idx_audit_created  ON audit_log(created_at DESC);

-- Blacklist de tokens JWT (logout invalidação)
CREATE TABLE jwt_blacklist (
    jti         VARCHAR(36) PRIMARY KEY,
    expira_em   TIMESTAMPTZ NOT NULL
);
