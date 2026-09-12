package br.com.keila.modules.auth;

import br.com.keila.modules.auth.dto.AuthResponse;
import br.com.keila.modules.auth.dto.LoginRequest;
import br.com.keila.modules.auth.dto.PinLoginRequest;
import br.com.keila.modules.auth.security.JwtService;
import br.com.keila.modules.auth.security.TokenBlacklistService;
import br.com.keila.modules.usuario.model.Usuario;
import br.com.keila.modules.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha incorretos."));

        return buildResponse(usuario);
    }

    public AuthResponse loginWithPin(PinLoginRequest request) {
        Usuario usuario = usuarioRepository.findByAtivoTrueAndPinHashIsNotNull().stream()
                .filter(u -> passwordEncoder.matches(request.pin(), u.getPinHash()))
                .findFirst()
                .orElseThrow(() -> new BadCredentialsException("PIN inválido."));

        return buildResponse(usuario);
    }

    public void logout(String token) {
        String jti = jwtService.extractJti(token);
        Instant expiration = jwtService.extractExpiration(token).toInstant();
        if (expiration.isAfter(Instant.now())) {
            tokenBlacklistService.blacklist(jti, expiration);
        }
    }

    private AuthResponse buildResponse(Usuario usuario) {
        String token = jwtService.generateToken(usuario);
        return new AuthResponse(token, usuario.getNome(), usuario.getPerfil().name(), usuario.getId());
    }
}
