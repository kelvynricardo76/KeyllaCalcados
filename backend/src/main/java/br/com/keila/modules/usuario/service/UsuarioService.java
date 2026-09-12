package br.com.keila.modules.usuario.service;

import br.com.keila.modules.usuario.dto.AtualizarUsuarioRequest;
import br.com.keila.modules.usuario.dto.CriarUsuarioRequest;
import br.com.keila.modules.usuario.dto.UsuarioResponse;
import br.com.keila.modules.usuario.model.Usuario;
import br.com.keila.modules.usuario.repository.UsuarioRepository;
import br.com.keila.shared.exception.EntidadeNaoEncontradaException;
import br.com.keila.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAllByOrderByNomeAsc().stream().map(this::toResponse).toList();
    }

    public UsuarioResponse criar(CriarUsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RegraNegocioException("Já existe um usuário com o e-mail '" + request.email() + "'.");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .ativo(true)
                .build();

        return toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse atualizar(Long id, AtualizarUsuarioRequest request) {
        Usuario usuario = buscarOuFalhar(id);
        if (usuarioRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new RegraNegocioException("Já existe um usuário com o e-mail '" + request.email() + "'.");
        }

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setPerfil(request.perfil());
        if (StringUtils.hasText(request.novaSenha())) {
            usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse alterarAtivo(Long id, boolean ativo) {
        Usuario usuario = buscarOuFalhar(id);
        usuario.setAtivo(ativo);
        return toResponse(usuarioRepository.save(usuario));
    }

    private Usuario buscarOuFalhar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado: " + id));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), u.isAtivo());
    }
}
