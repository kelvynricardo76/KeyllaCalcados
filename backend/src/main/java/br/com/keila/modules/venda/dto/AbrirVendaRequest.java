package br.com.keila.modules.venda.dto;

import jakarta.validation.constraints.NotNull;

public record AbrirVendaRequest(
        @NotNull Long sessaoId,
        @NotNull Long lojaId,
        Long clienteId
) {
}
