package br.com.keila.modules.relatorio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioVendasResponse(
        LocalDate inicio,
        LocalDate fim,
        BigDecimal faturamentoTotal,
        long quantidadeVendas,
        BigDecimal ticketMedio,
        List<VendaResumoResponse> vendas,
        List<ProdutoRankingResponse> maisVendidos,
        List<ProdutoRankingResponse> menosVendidos,
        List<String> produtosSemVenda
) {
}
