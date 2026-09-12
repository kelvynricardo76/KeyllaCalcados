package br.com.keila.modules.venda.dto;

import br.com.keila.modules.venda.model.FormaPagamento;

import java.math.BigDecimal;

public record PagamentoVendaResponse(
        Long id,
        FormaPagamento forma,
        BigDecimal valor,
        int parcelas,
        String referencia
) {
}
