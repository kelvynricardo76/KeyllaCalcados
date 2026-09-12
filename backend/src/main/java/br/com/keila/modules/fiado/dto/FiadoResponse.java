package br.com.keila.modules.fiado.dto;

import br.com.keila.modules.fiado.model.StatusFiado;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FiadoResponse(
        Long id,
        Long clienteId,
        String clienteNome,
        Long lojaId,
        String lojaNome,
        BigDecimal valorTotal,
        BigDecimal valorPago,
        BigDecimal valorRestante,
        LocalDate dataLancamento,
        LocalDate dataVencimento,
        StatusFiado status,
        boolean vencido,
        String observacoes
) {
}
