package br.com.keila.modules.produto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoRequest(
        @NotBlank @Size(max = 200) String nome,
        String descricao,
        Long marcaId,
        Long categoriaId,
        String codigoBarras,
        String sku,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal precoCusto,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal precoVenda,
        boolean temGrade,
        String fotoPrincipalUrl,
        LocalDate dataValidade
) {
}
