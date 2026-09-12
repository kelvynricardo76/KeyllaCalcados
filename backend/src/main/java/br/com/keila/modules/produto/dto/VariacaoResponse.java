package br.com.keila.modules.produto.dto;

import java.math.BigDecimal;

public record VariacaoResponse(
        Long id,
        Long produtoId,
        String produtoNome,
        Long tamanhoId,
        String tamanhoValor,
        Long corId,
        String corNome,
        String corHex,
        String sku,
        String codigoBarras,
        BigDecimal precoCustoOverride,
        BigDecimal precoVendaOverride,
        boolean ativo
) {
}
