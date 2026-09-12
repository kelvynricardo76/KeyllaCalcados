package br.com.keila.modules.venda.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemDevolucaoRequest(
        @NotNull Long variacaoId,
        @NotNull @Positive Integer quantidade
) {
}
