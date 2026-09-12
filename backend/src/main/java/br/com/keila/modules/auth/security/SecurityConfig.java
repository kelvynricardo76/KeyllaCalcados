package br.com.keila.modules.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/**",
            "/api/v1/api-docs/**",
            "/api/v1/swagger-ui/**",
            "/actuator/health",
            "/h2-console/**"
    };

    /** Catálogo/estoque — perfil VENDEDOR pode consultar (GET) livremente. */
    private static final String[] VENDEDOR_CONSULTA_PATHS = {
            "/api/v1/produtos/**", "/api/v1/estoque/**", "/api/v1/marcas/**",
            "/api/v1/categorias/**", "/api/v1/cores/**", "/api/v1/tamanhos/**", "/api/v1/lojas/**"
    };

    /** Módulos totalmente fora do alcance do perfil VENDEDOR (vendas, caixa, financeiro, clientes...). */
    private static final String[] VENDEDOR_RESTRITO_PATHS = {
            "/api/v1/produtos/**", "/api/v1/estoque/**", "/api/v1/marcas/**", "/api/v1/categorias/**",
            "/api/v1/cores/**", "/api/v1/tamanhos/**", "/api/v1/lojas/**", "/api/v1/vendas/**",
            "/api/v1/caixas/**", "/api/v1/sessoes/**", "/api/v1/clientes/**", "/api/v1/fiados/**",
            "/api/v1/fornecedores/**", "/api/v1/compras/**", "/api/v1/relatorios/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers("/api/v1/usuarios/**").hasRole("ADMIN")
                        // Vendedor só pode consultar (GET) catálogo e estoque — nada de vendas, caixa, clientes, financeiro etc.
                        .requestMatchers(HttpMethod.GET, VENDEDOR_CONSULTA_PATHS).authenticated()
                        .requestMatchers(VENDEDOR_RESTRITO_PATHS)
                                .hasAnyRole("ADMIN", "GERENTE", "CAIXA", "ESTOQUISTA")
                        .anyRequest().authenticated())
                // Console do H2 roda dentro de um <frame>; sem isso o navegador bloqueia por X-Frame-Options.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
