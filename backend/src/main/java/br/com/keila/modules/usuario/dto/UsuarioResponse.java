package br.com.keila.modules.usuario.dto;

import br.com.keila.modules.usuario.model.PerfilUsuario;

public record UsuarioResponse(Long id, String nome, String email, PerfilUsuario perfil, boolean ativo) {
}
