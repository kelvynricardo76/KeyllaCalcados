package br.com.keila.modules.estoque.dto;

import br.com.keila.modules.estoque.model.TipoMovEstoque;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AjusteEstoqueRequest(
        @NotNull Long variacaoId,
        @NotNull Long lojaId,
        @NotNull TipoMovEstoque tipo,
        @NotNull @Positive Integer quantidade,
        String motivo
) {
}
