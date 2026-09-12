package br.com.keila.modules.estoque.dto;

import br.com.keila.modules.estoque.model.TipoMovEstoque;

import java.time.OffsetDateTime;

public record MovimentacaoResponse(
        Long id,
        TipoMovEstoque tipo,
        int quantidade,
        int quantidadeAnterior,
        int quantidadePosterior,
        String motivo,
        String usuarioNome,
        OffsetDateTime createdAt
) {
}
