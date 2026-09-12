package br.com.keila.modules.venda.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public record FinalizarVendaRequest(
        @DecimalMin(value = "0") BigDecimal descontoGeral,
        @NotEmpty @Valid List<PagamentoInput> pagamentos,
        Integer fiadoVencimentoDias
) {
}
