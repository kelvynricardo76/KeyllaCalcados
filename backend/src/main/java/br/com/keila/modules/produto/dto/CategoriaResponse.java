package br.com.keila.modules.produto.dto;

public record CategoriaResponse(
        Long id,
        String nome,
        Long categoriaPaiId,
        String categoriaPaiNome,
        String descricao,
        Integer ordem,
        boolean ativo
) {
}
