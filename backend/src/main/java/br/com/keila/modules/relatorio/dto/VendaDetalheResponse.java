package br.com.keila.modules.relatorio.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record VendaDetalheResponse(
        Long id,
        Instant createdAt,
        String clienteNome,
        String clienteTelefone,
        String usuarioNome,
        String lojaNome,
        BigDecimal subtotal,
        BigDecimal descontoGeral,
        BigDecimal valorTotal,
        BigDecimal troco,
        String observacoes,
        List<ItemVendaResumo> itens,
        List<PagamentoResumo> pagamentos
) {
}
