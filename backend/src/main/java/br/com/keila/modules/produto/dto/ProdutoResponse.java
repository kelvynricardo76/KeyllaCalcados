package br.com.keila.modules.produto.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        Long marcaId,
        String marcaNome,
        Long categoriaId,
        String categoriaNome,
        String codigoBarras,
        String sku,
        BigDecimal precoCusto,
        BigDecimal precoVenda,
        BigDecimal margemPercentual,
        boolean temGrade,
        String fotoPrincipalUrl,
        LocalDate dataValidade,
        boolean ativo
) {
}
