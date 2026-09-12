# Keila Calçados — Sistema PDV, Estoque e Gestão

Sistema completo de gestão para uma loja de calçados: catálogo com grade de
tamanho/cor, controle de estoque com kardex, ponto de venda (PDV) com frente
de caixa, controle de fiado, trocas/devoluções, fornecedores/compras,
relatórios e gestão de usuários.

Full-stack: **Angular** no frontend e **Spring Boot** no backend, com banco de
dados H2 persistido em arquivo local — **sem Docker, sem Postgres, sem Redis**.

## Funcionalidades

- **Dashboard** — faturamento do dia/ontem, ticket médio, fiados em aberto e
  vencidos, alertas de estoque baixo e vendas por hora.
- **Produtos & Catálogo** — categorias, marcas, produtos e grade de variações
  (tamanho × cor).
- **Estoque** — saldo por variação/loja, kardex completo de movimentações,
  ajustes manuais de entrada/saída.
- **PDV (Frente de Caixa)** — abertura/fechamento de sessão com conferência de
  sobra/falta, sangria/suprimento, carrinho de venda, múltiplas formas de
  pagamento (dinheiro, débito, crédito, PIX, fiado), cálculo de troco.
- **Clientes & Fiado** — cadastro de clientes, lançamento e recebimento de
  fiado, controle de vencidos.
- **Trocas & Devoluções** — devolução de itens de uma venda finalizada, com
  estorno automático de estoque.
- **Fornecedores & Compras** — cadastro de fornecedores, registro de compras
  com itens e recebimento com entrada em estoque.
- **Relatórios** — vendas por período, ticket médio, produtos mais vendidos.
- **Usuários** — gestão de usuários e perfis de acesso (ADMIN, GERENTE, CAIXA,
  ESTOQUISTA), restrita a administradores.

## Tecnologias

**Backend**
- Java 21 + Spring Boot 3.3
- Spring Data JPA / Hibernate (schema gerenciado automaticamente, sem
  migrations)
- Banco de dados H2 (arquivo local, sem necessidade de instalar SGBD)
- Spring Security + JWT (blacklist de logout mantida em memória)

**Frontend**
- Angular 18 (standalone components, roteamento com lazy loading por módulo)
- TypeScript, RxJS, Signals
- CSS puro (sem framework de UI), design system próprio

## Estrutura do projeto

```
KeilaCalçados/
├── backend/     # API REST em Spring Boot
│   └── src/main/java/br/com/keila/
│       ├── modules/        # Um pacote por domínio (produto, estoque, venda,
│       │                     caixa, fiado, cliente, fornecedor, usuario, ...)
│       │   └── <modulo>/model | repository | service | controller | dto
│       ├── bootstrap/      # Carga inicial de dados de exemplo (DataSeeder)
│       └── shared/         # Exceções e utilitários compartilhados
└── frontend/    # Aplicação Angular
    └── src/app/
        ├── core/            # Auth, guards, interceptors, serviços compartilhados
        └── modules/
            ├── auth/        # Login (e-mail/senha, PIN)
            ├── pdv/         # Ponto de venda
            └── backoffice/  # Dashboard, produtos, estoque, clientes, etc.
```

## Como rodar o projeto

### Pré-requisitos

- [Java 21](https://adoptium.net/) ou superior
- [Node.js](https://nodejs.org/) 18+ e npm

Não é necessário instalar Maven — o projeto já traz o Maven Wrapper (`mvnw`).
Também não é necessário Docker, Postgres ou Redis.

### 1. Backend (porta 8080)

```bash
cd backend
./mvnw spring-boot:run
```

O banco H2 é criado automaticamente em `backend/data/` na primeira execução,
já populado com loja, usuário admin, categorias, marcas, tamanhos e cores
padrão.

### 2. Frontend (porta 4200)

```bash
cd frontend
npm install
npm start
```

Acesse **http://localhost:4200**.

### Login de acesso

```
Usuário: admin
Senha:   admin
```

> A tela de login também tem um botão "Entrar em modo demonstração (sem
> backend)", visível apenas fora de produção — útil só para navegar pela UI
> quando o backend não está no ar. Com o backend rodando, use o login normal.

## API

A API REST fica disponível em `http://localhost:8080/api/v1`. Um console web
do banco H2 fica disponível em `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:file:./data/keiladb`, usuário `sa`, sem senha).

| Recurso | Base | Descrição |
|---|---|---|
| Autenticação | `/api/v1/auth` | Login por e-mail/senha ou PIN, logout |
| Produtos | `/api/v1/produtos`, `/marcas`, `/categorias`, `/tamanhos`, `/cores` | Catálogo e grade de variações |
| Estoque | `/api/v1/estoque` | Saldo, ajustes, kardex |
| Vendas (PDV) | `/api/v1/vendas` | Abrir, adicionar itens, finalizar, cancelar |
| Devoluções | `/api/v1/vendas/{id}/devolucoes` | Trocas e devoluções |
| Caixa | `/api/v1/caixas`, `/sessoes` | Abrir/fechar sessão, sangria/suprimento |
| Clientes | `/api/v1/clientes` | CRUD |
| Fiado | `/api/v1/fiados` | Lançamento e recebimento de pagamentos |
| Fornecedores/Compras | `/api/v1/fornecedores`, `/compras` | CRUD e recebimento de mercadoria |
| Relatórios | `/api/v1/relatorios/dashboard`, `/vendas` | KPIs e relatório por período |
| Usuários | `/api/v1/usuarios` | Gestão de usuários (restrito a ADMIN) |

## Testes

```bash
cd backend
./mvnw test
```

17 testes unitários cobrem a lógica de negócio mais crítica: movimentação de
estoque, pagamento de fiado, fechamento de caixa e finalização de venda
(validação de pagamento, troco e geração de fiado).
