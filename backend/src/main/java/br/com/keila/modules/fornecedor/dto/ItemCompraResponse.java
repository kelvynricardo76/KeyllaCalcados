package br.com.keila.modules.fornecedor.dto;

import java.math.BigDecimal;

public record ItemCompraResponse(
        Long id,
        Long variacaoId,
        String produtoNome,
        String tamanhoValor,
        String corNome,
        String sku,
        int quantidade,
        int quantidadeRecebida,
        BigDecimal precoCustoUnitario,
        BigDecimal subtotal
) {
}
