package br.com.keila.modules.caixa.dto;

import br.com.keila.modules.caixa.model.StatusSessao;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SessaoResponse(
        Long id,
        Long caixaId,
        String caixaNome,
        String usuarioNome,
        OffsetDateTime dataAbertura,
        BigDecimal valorAbertura,
        OffsetDateTime dataFechamento,
        BigDecimal valorFechamentoInformado,
        BigDecimal valorFechamentoCalculado,
        BigDecimal diferenca,
        StatusSessao status,
        String observacoes
) {
}
