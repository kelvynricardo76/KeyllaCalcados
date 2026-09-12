package br.com.keila.modules.venda.dto;

import br.com.keila.modules.venda.model.TipoDevolucao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DevolucaoRequest(
        @NotNull TipoDevolucao tipo,
        @NotBlank String motivo,
        @NotEmpty @Valid List<ItemDevolucaoRequest> itens
) {
}
