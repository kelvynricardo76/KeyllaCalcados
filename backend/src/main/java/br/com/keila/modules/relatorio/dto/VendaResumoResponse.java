package br.com.keila.modules.relatorio.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record VendaResumoResponse(
        Long id,
        OffsetDateTime createdAt,
        String clienteNome,
        String usuarioNome,
        BigDecimal valorTotal
) {
}
