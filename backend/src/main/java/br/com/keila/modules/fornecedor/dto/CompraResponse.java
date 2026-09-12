package br.com.keila.modules.fornecedor.dto;

import br.com.keila.modules.fornecedor.model.StatusCompra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CompraResponse(
        Long id,
        Long fornecedorId,
        String fornecedorNome,
        Long lojaId,
        LocalDate dataCompra,
        LocalDate dataEntrega,
        String numeroNf,
        StatusCompra status,
        BigDecimal valorTotal,
        String observacoes,
        List<ItemCompraResponse> itens
) {
}
