package br.com.keila.modules.venda.dto;

import br.com.keila.modules.venda.model.FormaPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PagamentoInput(
        @NotNull FormaPagamento forma,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        Integer parcelas,
        String referencia
) {
}
