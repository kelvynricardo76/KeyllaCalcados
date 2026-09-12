package br.com.keila.modules.produto.dto;

public record MarcaResponse(
        Long id,
        String nome,
        String descricao,
        String logoUrl,
        boolean ativo
) {
}
