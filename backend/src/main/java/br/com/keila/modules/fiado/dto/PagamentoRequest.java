package br.com.keila.modules.fiado.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PagamentoRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @NotBlank String formaPagamento,
        String observacoes
) {
}
