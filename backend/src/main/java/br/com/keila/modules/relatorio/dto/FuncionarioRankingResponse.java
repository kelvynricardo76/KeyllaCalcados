package br.com.keila.modules.relatorio.dto;

import java.math.BigDecimal;

public record FuncionarioRankingResponse(
        Long usuarioId,
        String nome,
        String perfil,
        long quantidadeVendas,
        BigDecimal valorTotal,
        BigDecimal ticketMedio
) {
}
