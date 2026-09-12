package br.com.keila.modules.relatorio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Demonstrativo financeiro simplificado do período: do faturamento bruto até o
 * lucro líquido, passando por descontos, devoluções, CMV e despesas de caixa.
 */
public record RelatorioFinanceiroResponse(
        LocalDate inicio,
        LocalDate fim,
        long quantidadeVendas,
        BigDecimal ticketMedio,
        BigDecimal faturamentoBruto,
        BigDecimal descontos,
        BigDecimal devolucoes,
        BigDecimal faturamentoLiquido,
        BigDecimal cmv,
        BigDecimal lucroBruto,
        BigDecimal margemBruta,
        BigDecimal despesas,
        BigDecimal receitasAvulsas,
        BigDecimal lucroLiquido,
        BigDecimal margemLiquida
) {
}
