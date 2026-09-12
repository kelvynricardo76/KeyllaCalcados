package br.com.keila.modules.estoque.dto;

import java.time.OffsetDateTime;

public record EstoqueResponse(
        Long id,
        Long variacaoId,
        String produtoNome,
        String tamanhoValor,
        String corNome,
        String sku,
        Long lojaId,
        String lojaNome,
        int quantidade,
        int estoqueMinimo,
        String statusEstoque,
        OffsetDateTime updatedAt
) {
}
