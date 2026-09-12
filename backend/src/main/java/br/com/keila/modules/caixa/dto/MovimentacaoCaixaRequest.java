package br.com.keila.modules.caixa.dto;

import br.com.keila.modules.caixa.model.TipoMovCaixa;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MovimentacaoCaixaRequest(
        @NotNull TipoMovCaixa tipo,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @NotBlank String descricao,
        String categoria
) {
}
