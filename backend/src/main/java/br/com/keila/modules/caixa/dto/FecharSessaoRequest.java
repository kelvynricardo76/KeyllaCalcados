package br.com.keila.modules.caixa.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FecharSessaoRequest(
        @NotNull @DecimalMin(value = "0") BigDecimal valorFechamentoInformado,
        String observacoes
) {
}
