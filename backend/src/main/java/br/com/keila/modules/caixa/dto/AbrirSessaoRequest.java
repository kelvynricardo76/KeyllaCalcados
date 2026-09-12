package br.com.keila.modules.caixa.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AbrirSessaoRequest(
        @NotNull @DecimalMin(value = "0") BigDecimal valorAbertura
) {
}
