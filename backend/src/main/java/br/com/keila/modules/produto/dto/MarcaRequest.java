package br.com.keila.modules.produto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MarcaRequest(
        @NotBlank @Size(max = 80) String nome,
        String descricao,
        String logoUrl
) {
}
