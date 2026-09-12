package br.com.keila.modules.estoque.model;

/** Espelha o tipo ENUM `tipo_mov_estoque` criado em V3__create_estoque.sql. */
public enum TipoMovEstoque {
    ENTRADA,
    SAIDA,
    AJUSTE_POSITIVO,
    AJUSTE_NEGATIVO,
    TRANSFERENCIA_SAIDA,
    TRANSFERENCIA_ENTRADA,
    DEVOLUCAO
}
