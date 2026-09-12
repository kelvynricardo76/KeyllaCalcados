package br.com.keila.modules.venda.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AdicionarItemRequest(
        @NotNull Long variacaoId,
        @NotNull @Positive Integer quantidade,
        @DecimalMin(value = "0") BigDecimal descontoItem
) {
}
