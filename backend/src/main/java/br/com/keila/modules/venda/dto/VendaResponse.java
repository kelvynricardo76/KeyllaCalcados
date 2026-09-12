package br.com.keila.modules.venda.dto;

import br.com.keila.modules.venda.model.StatusVenda;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record VendaResponse(
        Long id,
        Long sessaoId,
        Long clienteId,
        String clienteNome,
        String usuarioNome,
        Long lojaId,
        StatusVenda status,
        BigDecimal subtotal,
        BigDecimal descontoGeral,
        BigDecimal valorTotal,
        BigDecimal troco,
        String observacoes,
        List<ItemVendaResponse> itens,
        List<PagamentoVendaResponse> pagamentos,
        Instant createdAt
) {
}
