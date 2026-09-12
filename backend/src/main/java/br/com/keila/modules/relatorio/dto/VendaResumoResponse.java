package br.com.keila.modules.relatorio.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record VendaResumoResponse(
        Long id,
        Instant createdAt,
        String clienteNome,
        String usuarioNome,
        BigDecimal valorTotal
) {
}
