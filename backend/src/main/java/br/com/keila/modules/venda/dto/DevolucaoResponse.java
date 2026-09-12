package br.com.keila.modules.venda.dto;

import br.com.keila.modules.venda.model.TipoDevolucao;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record DevolucaoResponse(
        Long id,
        Long vendaOrigemId,
        String clienteNome,
        TipoDevolucao tipo,
        String motivo,
        BigDecimal valorDevolvido,
        String usuarioNome,
        OffsetDateTime createdAt,
        List<ItemDevolvidoResumo> itens
) {
}
