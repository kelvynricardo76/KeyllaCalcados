package br.com.keila.modules.usuario.model;

/** Espelha o tipo ENUM `perfil_usuario` criado em V1__create_usuarios.sql. */
public enum PerfilUsuario {
    ADMIN,
    GERENTE,
    CAIXA,
    ESTOQUISTA,
    /** Só pode consultar o estoque disponível (sem acesso a vendas, clientes, financeiro etc.). */
    VENDEDOR
}
