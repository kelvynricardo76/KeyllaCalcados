package br.com.keila.modules.auth.security;

import br.com.keila.modules.usuario.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lê o Bearer token de cada requisição, valida contra a blacklist em memória
 * (populada no logout) e popula o SecurityContext quando válido.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String jti = jwtService.extractJti(token);
            if (tokenBlacklistService.isBlacklisted(jti)) {
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtService.extractEmail(token);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                usuarioRepository.findByEmail(email)
                        .filter(usuario -> jwtService.isTokenValid(token, usuario.getUsername()))
                        .ifPresent(usuario -> {
                            var authToken = new UsernamePasswordAuthenticationToken(
                                    usuario, null, usuario.getAuthorities());
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        });
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            // Token ausente/expirado/inválido → requisição segue anônima e é barrada pela SecurityConfig
        }

        filterChain.doFilter(request, response);
    }
}
