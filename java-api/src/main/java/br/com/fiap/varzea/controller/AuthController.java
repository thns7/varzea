package br.com.fiap.varzea.controller;

import br.com.fiap.varzea.bo.MoradorBO;
import br.com.fiap.varzea.dto.Requisicoes.Login;
import br.com.fiap.varzea.dto.Respostas.Token;
import br.com.fiap.varzea.model.Morador;
import br.com.fiap.varzea.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Rota pública de login: confere e-mail e senha e devolve o token JWT. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final MoradorBO moradorBO;
    private final JwtUtil jwtUtil;

    public AuthController(MoradorBO moradorBO, JwtUtil jwtUtil) {
        this.moradorBO = moradorBO;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Token login(@RequestBody @Valid Login dados) {
        Morador morador = moradorBO.autenticar(dados.email(), dados.senha());
        return new Token(jwtUtil.gerarToken(morador), "Bearer", morador.getId(), morador.getPerfil().name());
    }
}
