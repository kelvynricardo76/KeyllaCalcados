package br.com.keila.modules.usuario.dto;

import br.com.keila.modules.usuario.model.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarUsuarioRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email String email,
        @NotNull PerfilUsuario perfil,
        /** Em branco = mantém a senha atual. */
        String novaSenha
) {
}
