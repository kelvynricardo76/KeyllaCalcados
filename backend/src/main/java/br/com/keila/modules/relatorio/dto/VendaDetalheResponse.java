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
        BigDecimal valorTotal,
        List<ItemVendaResumo> itens
) {
}
