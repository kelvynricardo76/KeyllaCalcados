package br.com.keila.modules.estoque.dto;

import java.time.OffsetDateTime;

public record EstoqueResponse(
        Long id,
        Long variacaoId,
        Long produtoId,
        String produtoNome,
        String fotoPrincipalUrl,
        Long marcaId,
        String marcaNome,
        Long categoriaId,
        String categoriaNome,
        String tamanhoValor,
        Long corId,
        String corNome,
        String corHex,
        String sku,
        String codigoBarras,
        Long lojaId,
        String lojaNome,
        int quantidade,
        int estoqueMinimo,
        String statusEstoque,
        OffsetDateTime updatedAt
) {
}
