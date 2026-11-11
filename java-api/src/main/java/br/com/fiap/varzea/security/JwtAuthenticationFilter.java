package br.com.fiap.varzea.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * A cada requisição, lê o cabeçalho Authorization, valida o token e informa ao Spring Security
 * quem é o morador (o id dele) e qual o seu perfil.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RespostaErroJson respostaErro;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, RespostaErroJson respostaErro) {
        this.jwtUtil = jwtUtil;
        this.respostaErro = respostaErro;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String cabecalho = request.getHeader("Authorization");

        if (cabecalho != null && cabecalho.startsWith("Bearer ")) {
            try {
                Claims claims = jwtUtil.validarToken(cabecalho.substring(7));
                Long moradorId = Long.valueOf(claims.getSubject());
                String role = claims.get("role", String.class);
                var autenticacao = new UsernamePasswordAuthenticationToken(
                        moradorId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            } catch (Exception e) {
                respostaErro.escrever(response, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido ou expirado.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
