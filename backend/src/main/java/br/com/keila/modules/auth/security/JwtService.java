package br.com.keila.modules.auth.security;

import br.com.keila.modules.usuario.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Serviço responsável por gerar, validar e ler tokens JWT.
 *
 * Decisão: usamos HS256 com chave de 256 bits configurável via variável de ambiente.
 * O campo "jti" (JWT ID) é um UUID único por token, usado na blacklist de logout.
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        // Base64-encode a chave para garantir comprimento mínimo de 256 bits
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(secret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** Gera token JWT para o usuário com claims customizados (perfil, lojaId). */
    public String generateToken(Usuario usuario) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())           // jti para blacklist
                .subject(usuario.getEmail())
                .claims(Map.of(
                        "userId",  usuario.getId(),
                        "perfil",  usuario.getPerfil().name(),
                        "nome",    usuario.getNome()
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /** Extrai o e-mail (subject) do token. */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /** Extrai o JTI (JWT ID) — usado para invalidar tokens no logout. */
    public String extractJti(String token) {
        return parseClaims(token).getId();
    }

    /** Extrai a data de expiração — usada para definir TTL na blacklist Redis. */
    public Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    /** Valida assinatura e expiração do token. Lança JwtException se inválido. */
    public boolean isTokenValid(String token, String userEmail) {
        final String email = extractEmail(token);
        return email.equals(userEmail) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
