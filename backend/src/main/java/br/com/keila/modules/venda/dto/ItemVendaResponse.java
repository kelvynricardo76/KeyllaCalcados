package br.com.keila.modules.venda.dto;

import java.math.BigDecimal;

public record ItemVendaResponse(
        Long id,
        Long variacaoId,
        String nomeProduto,
        String sku,
        String tamanho,
        String cor,
        int quantidade,
        BigDecimal precoUnitario,
        BigDecimal descontoItem,
        BigDecimal subtotal
) {
}
