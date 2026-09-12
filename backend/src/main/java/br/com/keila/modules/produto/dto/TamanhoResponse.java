package br.com.keila.modules.produto.dto;

import br.com.keila.modules.produto.model.TipoTamanho;

public record TamanhoResponse(Long id, String valor, TipoTamanho tipo, Integer ordem) {
}
