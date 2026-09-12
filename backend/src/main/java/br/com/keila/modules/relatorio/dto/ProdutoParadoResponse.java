package br.com.keila.modules.relatorio.dto;

import java.time.Instant;

public record ProdutoParadoResponse(
        Long produtoId,
        String nomeProduto,
        String marcaNome,
        Instant ultimaVenda,
        Long diasSemVenda,
        int quantidadeEmEstoque
) {
}
