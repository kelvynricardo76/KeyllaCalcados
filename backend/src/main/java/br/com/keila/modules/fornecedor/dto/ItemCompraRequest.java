package br.com.keila.modules.fornecedor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ItemCompraRequest(
        @NotNull Long variacaoId,
        @NotNull @Positive Integer quantidade,
        @NotNull @DecimalMin(value = "0") BigDecimal precoCustoUnitario
) {
}
