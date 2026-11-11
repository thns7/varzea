package br.com.fiap.varzea.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Define as rotas públicas, a API sem sessão (STATELESS) e onde entra o filtro JWT. */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtil jwtUtil, RespostaErroJson respostaErro) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Login e cadastro são públicos.
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/moradores").permitAll()
                // Consultas do campeonato são públicas, para a página de vendas e a tabela.
                .requestMatchers(HttpMethod.GET, "/api/status", "/api/distritos", "/api/categorias", "/api/servicos/**",
                        "/api/rodadas/**", "/api/gols", "/api/classificacao").permitAll()
                .requestMatchers("/error").permitAll()
                // Todo o resto exige token.
                .anyRequest().authenticated())
            .exceptionHandling(erro -> erro
                .authenticationEntryPoint((req, res, e) -> respostaErro.escrever(res, 401, "Faça login para acessar."))
                .accessDeniedHandler((req, res, e) -> respostaErro.escrever(res, 403, "Você não tem permissão para esta operação.")))
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, respostaErro), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Senhas guardadas só como hash BCrypt. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
