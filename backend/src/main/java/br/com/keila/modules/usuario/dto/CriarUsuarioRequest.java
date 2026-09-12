package br.com.keila.modules.usuario.dto;

import br.com.keila.modules.usuario.model.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarUsuarioRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "A senha deve ter ao menos 6 caracteres") String senha,
        @NotNull PerfilUsuario perfil
) {
}
