package br.com.keila.modules.relatorio.dto;

import java.math.BigDecimal;

public record ClienteRankingResponse(
        Long clienteId,
        String nome,
        String telefone,
        long quantidadeCompras,
        BigDecimal valorTotal,
        BigDecimal ticketMedio
) {
}
