package br.com.keila.modules.relatorio.dto;

import java.math.BigDecimal;

public record ProdutoRankingResponse(String nomeProduto, long quantidadeVendida, BigDecimal valorTotal) {
}
