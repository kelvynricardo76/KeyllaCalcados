package br.com.keila.modules.caixa.dto;

public record CaixaResponse(Long id, String nome, boolean ativo, Long lojaId, Long sessaoAbertaId) {
}
