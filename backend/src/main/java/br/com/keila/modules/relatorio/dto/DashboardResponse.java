package br.com.keila.modules.relatorio.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal faturamentoHoje,
        BigDecimal faturamentoOntem,
        long vendasHoje,
        BigDecimal ticketMedio,
        BigDecimal fiadosEmAberto,
        long fiadosVencidos,
        long estoqueBaixo,
        List<PontoVendaHora> vendasPorHora
) {
}
