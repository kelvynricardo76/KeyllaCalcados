package br.com.keila.modules.fiado.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PagamentoResponse(
        Long id,
        BigDecimal valor,
        String formaPagamento,
        LocalDate dataPagamento,
        String usuarioNome,
        String observacoes,
        OffsetDateTime createdAt
) {
}
