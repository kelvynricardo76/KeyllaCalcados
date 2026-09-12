package br.com.keila.modules.fiado.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FiadoRequest(
        @NotNull Long clienteId,
        @NotNull Long lojaId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valorTotal,
        @NotNull LocalDate dataVencimento,
        String observacoes
) {
}
