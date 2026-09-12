package br.com.keila.modules.produto.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record VariacaoRequest(
        Long tamanhoId,
        Long corId,
        @NotBlank String sku,
        String codigoBarras,
        BigDecimal precoCustoOverride,
        BigDecimal precoVendaOverride
) {
}
