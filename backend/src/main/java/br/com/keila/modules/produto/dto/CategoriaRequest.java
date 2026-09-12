package br.com.keila.modules.produto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank @Size(max = 80) String nome,
        Long categoriaPaiId,
        String descricao,
        Integer ordem
) {
}
