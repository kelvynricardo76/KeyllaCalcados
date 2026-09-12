package br.com.keila.modules.caixa.dto;

import br.com.keila.modules.caixa.model.TipoMovCaixa;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MovimentacaoCaixaResponse(
        Long id,
        TipoMovCaixa tipo,
        BigDecimal valor,
        String descricao,
        String categoria,
        String usuarioNome,
        OffsetDateTime createdAt
) {
}
