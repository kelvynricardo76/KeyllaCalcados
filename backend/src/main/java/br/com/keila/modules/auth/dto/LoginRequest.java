package br.com.keila.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login aceita tanto um e-mail real quanto um usuário simples (ex.: "admin"),
 * por isso o campo não valida formato de e-mail aqui — só na criação de
 * usuários (ver CriarUsuarioRequest), onde um e-mail de verdade é exigido.
 */
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String senha
) {
}
