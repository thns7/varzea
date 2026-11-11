package br.com.fiap.varzea.security;

import br.com.fiap.varzea.model.Morador;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;

/** Gera e valida os tokens JWT. A chave vem da variável de ambiente JWT_SEGREDO, nunca do código. */
@Component
public class JwtUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtUtil(@Value("${varzea.jwt.segredo}") String segredo, @Value("${varzea.jwt.expiracao-ms}") long expiracaoMs) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            // Sem uma chave configurada, gera uma temporária: os tokens deixam de valer quando a API reinicia.
            LOG.warn("JWT_SEGREDO ausente ou curto: usando uma chave temporária.");
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
        }
        this.chave = Keys.hmacShaKeyFor(bytes);
        this.expiracaoMs = expiracaoMs;
    }

    public String gerarToken(Morador morador) {
        return Jwts.builder()
                .subject(String.valueOf(morador.getId()))
                .claim("role", morador.getPerfil().name())
                .expiration(new Date(System.currentTimeMillis() + expiracaoMs))
                .signWith(chave, Jwts.SIG.HS256)
                .compact();
    }

    public Claims validarToken(String token) {
        return Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
    }
}
