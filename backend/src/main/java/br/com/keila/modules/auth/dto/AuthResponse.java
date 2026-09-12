package br.com.keila.modules.auth.dto;

public record AuthResponse(
        String token,
        String nome,
        String perfil,
        Long userId
) {
}
