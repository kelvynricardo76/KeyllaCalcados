package br.com.keila.modules.fornecedor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CompraRequest(
        Long fornecedorId,
        @NotNull Long lojaId,
        LocalDate dataEntrega,
        String numeroNf,
        String observacoes,
        @NotEmpty @Valid List<ItemCompraRequest> itens
) {
}
