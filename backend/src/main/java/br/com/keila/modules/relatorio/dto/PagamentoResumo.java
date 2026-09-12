package br.com.keila.modules.relatorio.dto;

import java.math.BigDecimal;

public record PagamentoResumo(
        String forma,
        BigDecimal valor,
        int parcelas,
        String referencia
) {
}
