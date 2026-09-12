-- ═══════════════════════════════════════════════════════════════
-- V7 — Seed de dados iniciais
-- ═══════════════════════════════════════════════════════════════

-- ─── Loja padrão ─────────────────────────────────────────────
INSERT INTO lojas (nome, cnpj, telefone, endereco) VALUES
('Keila Calçados', '00.000.000/0001-00', '(00) 00000-0000', 'Rua das Flores, 100 - Centro');

-- ─── Usuário Admin padrão ─────────────────────────────────────
-- Senha: Admin@123 (bcrypt hash)
-- IMPORTANTE: alterar imediatamente em produção via backoffice
INSERT INTO usuarios (nome, email, senha_hash, perfil, ativo) VALUES
('Administrador', 'admin@keila.com.br',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewWb7pnMm.GY0Ayi',
 'ADMIN', TRUE);

-- ─── Categorias raiz ─────────────────────────────────────────
INSERT INTO categorias (nome, descricao, ordem) VALUES
('Calçados',    'Todos os tipos de calçados', 1),
('Acessórios',  'Todos os acessórios', 2);

-- Subcategorias de Calçados
INSERT INTO categorias (nome, categoria_pai_id, ordem) VALUES
('Tênis',             1, 1),
('Sandália',          1, 2),
('Bota',              1, 3),
('Chinelo',           1, 4),
('Sapato Social',     1, 5),
('Sapatilha',         1, 6);

-- Subcategorias de Acessórios
INSERT INTO categorias (nome, categoria_pai_id, ordem) VALUES
('Cinto',            2, 1),
('Carteira',         2, 2),
('Bolsa',            2, 3),
('Meia',             2, 4),
('Meião',            2, 5),
('Caneleira',        2, 6),
('Bola de Futebol',  2, 7),
('Boné',             2, 8),
('Mala de Viagem',   2, 9);

-- ─── Marcas comuns ───────────────────────────────────────────
INSERT INTO marcas (nome) VALUES
('Kenner'),
('Nike'),
('Adidas'),
('Puma'),
('Havaianas'),
('Skechers'),
('Moleca'),
('Rider'),
('Fila'),
('Reserva');

-- ─── Tamanhos (Calçados infantil → adulto) ──────────────────
INSERT INTO tamanhos (valor, tipo, ordem) VALUES
('22', 'NUMERO', 1), ('23', 'NUMERO', 2), ('24', 'NUMERO', 3),
('25', 'NUMERO', 4), ('26', 'NUMERO', 5), ('27', 'NUMERO', 6),
('28', 'NUMERO', 7), ('29', 'NUMERO', 8), ('30', 'NUMERO', 9),
('31', 'NUMERO', 10), ('32', 'NUMERO', 11), ('33', 'NUMERO', 12),
('34', 'NUMERO', 13), ('35', 'NUMERO', 14), ('36', 'NUMERO', 15),
('37', 'NUMERO', 16), ('38', 'NUMERO', 17), ('39', 'NUMERO', 18),
('40', 'NUMERO', 19), ('41', 'NUMERO', 20), ('42', 'NUMERO', 21),
('43', 'NUMERO', 22), ('44', 'NUMERO', 23), ('45', 'NUMERO', 24),
('46', 'NUMERO', 25), ('47', 'NUMERO', 26), ('48', 'NUMERO', 27),
('P',  'LETRA',  28), ('M',  'LETRA',  29), ('G',  'LETRA',  30),
('GG', 'LETRA',  31), ('XG', 'LETRA',  32),
('ÚNICO', 'UNICO', 99);

-- ─── Cores comuns ────────────────────────────────────────────
INSERT INTO cores (nome, hex_code) VALUES
('Preto',       '#000000'),
('Branco',      '#FFFFFF'),
('Vermelho',    '#DC2626'),
('Azul',        '#2563EB'),
('Azul Marinho','#1E3A8A'),
('Verde',       '#16A34A'),
('Amarelo',     '#EAB308'),
('Cinza',       '#6B7280'),
('Marrom',      '#92400E'),
('Rosa',        '#EC4899'),
('Laranja',     '#EA580C'),
('Bege',        '#D4A574'),
('Roxo',        '#7C3AED'),
('Vinho',       '#9F1239');

-- ─── Caixa padrão ────────────────────────────────────────────
INSERT INTO caixas (loja_id, nome, ativo) VALUES
(1, 'Caixa 1', TRUE),
(1, 'Caixa 2', TRUE),
(1, 'Caixa 3', TRUE);
