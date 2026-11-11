package br.com.fiap.varzea.security;

import br.com.fiap.varzea.dto.Respostas.Erro;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Escreve erros de segurança no mesmo formato JSON dos demais erros da API. */
@Component
public class RespostaErroJson {

    private final ObjectMapper json;

    public RespostaErroJson(ObjectMapper json) {
        this.json = json;
    }

    public void escrever(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        json.writeValue(response.getOutputStream(), new Erro(status, mensagem, List.of()));
    }
}
