package br.com.keila.modules.relatorio.dto;

import java.time.LocalDate;

public record ProdutoVencendoResponse(
        Long produtoId,
        String nomeProduto,
        String marcaNome,
        LocalDate dataValidade,
        long diasParaVencer,
        boolean vencido
) {
}
