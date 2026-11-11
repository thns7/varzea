package br.com.fiap.varzea;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testa a API inteira, de ponta a ponta, sobre os scripts reais do banco e com login JWT. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VarzeaApiTest {

    private static final int MOOCA = 1;
    private static final int TATUAPE = 2;
    private static final int GUIAS = 1;
    private static final int GOLS_GUIAS = 8;
    private static final String SENHA = "senha-de-teste-123";
    private static final String FOTO = "https://example.com/foto.jpg";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private JdbcTemplate jdbc;

    /** Morador criado pelo cadastro público, com o token do login. */
    private record Conta(long id, String token) {
    }

    private ResultActions enviar(MockHttpServletRequestBuilder req, String token, String corpo) throws Exception {
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        if (corpo != null) {
            req.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        return mvc.perform(req);
    }

    private String login(String email, String senha) throws Exception {
        String corpo = enviar(post("/api/auth/login"), null, "{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(corpo).get("token").asText();
    }

    private Conta novaConta(int distrito) throws Exception {
        String email = "teste-" + UUID.randomUUID() + "@example.com";
        String corpo = enviar(post("/api/moradores"), null,
                "{\"distritoId\":%d,\"nome\":\"Teste\",\"email\":\"%s\",\"senha\":\"%s\"}".formatted(distrito, email, SENHA))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.perfil").value("USER"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        return new Conta(json.readTree(corpo).get("id").asLong(), login(email, SENHA));
    }

    private Conta novoAdmin() throws Exception {
        Conta conta = novaConta(MOOCA);
        jdbc.update("UPDATE t_vz_morador SET ds_perfil = 'ADMIN' WHERE id_morador = ?", conta.id());
        String email = jdbc.queryForObject("SELECT ds_email FROM t_vz_morador WHERE id_morador = ?", String.class, conta.id());
        return new Conta(conta.id(), login(email, SENHA));
    }

    private long novoChamado(Conta autor) throws Exception {
        String corpo = enviar(post("/api/chamados"), autor.token(), "{\"servicoId\":%d,\"referencia\":\"Trecho de teste\"}".formatted(GUIAS))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(corpo).get("id").asLong();
    }

    private ResultActions resolver(long chamado, Conta quem) throws Exception {
        return enviar(post("/api/chamados/" + chamado + "/resolver"), quem.token(), null);
    }

    private ResultActions votar(long chamado, Conta quem, String resultado) throws Exception {
        return enviar(post("/api/chamados/" + chamado + "/confirmacoes"), quem.token(),
                "{\"resultado\":\"%s\",\"fotoUrl\":\"%s\"}".formatted(resultado, FOTO));
    }

    @Test
    void consultasPublicasRespondemSemToken() throws Exception {
        mvc.perform(get("/api/status")).andExpect(status().isOk()).andExpect(jsonPath("$.banco").value("conectado"));
        mvc.perform(get("/api/distritos")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(6));
        mvc.perform(get("/api/categorias")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(6));
        mvc.perform(get("/api/servicos")).andExpect(status().isOk()).andExpect(jsonPath("$[0].gols").value(GOLS_GUIAS));
        mvc.perform(get("/api/servicos/1")).andExpect(status().isOk()).andExpect(jsonPath("$.categoria.nome").value("Pavimento"));
        mvc.perform(get("/api/rodadas")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(5));
        mvc.perform(get("/api/rodadas/1/partidas")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].casa.nome").value("MOOCA")).andExpect(jsonPath("$[0].golsCasa").value(8))
                .andExpect(jsonPath("$[0].fora.nome").value("TATUAPE")).andExpect(jsonPath("$[0].golsFora").value(2));
        mvc.perform(get("/api/gols")).andExpect(status().isOk()).andExpect(jsonPath("$[0].distrito").exists());
        mvc.perform(get("/api/classificacao")).andExpect(status().isOk()).andExpect(jsonPath("$[0].posicao").value(1));
    }

    @Test
    void loginEPermissoes() throws Exception {
        enviar(post("/api/auth/login"), null, "{\"email\":\"bruno.exemplo@example.com\",\"senha\":\"varzea123\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").exists()).andExpect(jsonPath("$.perfil").value("USER"));
        enviar(post("/api/auth/login"), null, "{\"email\":\"bruno.exemplo@example.com\",\"senha\":\"errada\"}")
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.mensagem").value("Credenciais inválidas."));
        mvc.perform(get("/api/chamados")).andExpect(status().isUnauthorized());
        enviar(get("/api/chamados"), "token-falso", null).andExpect(status().isUnauthorized());

        Conta usuario = novaConta(MOOCA);
        Conta outro = novaConta(MOOCA);
        Conta admin = novoAdmin();
        enviar(get("/api/chamados"), usuario.token(), null).andExpect(status().isOk());
        enviar(get("/api/moradores"), usuario.token(), null).andExpect(status().isForbidden());
        enviar(get("/api/moradores"), admin.token(), null).andExpect(status().isOk());
        enviar(get("/api/moradores/eu"), usuario.token(), null).andExpect(jsonPath("$.id").value(usuario.id()));
        enviar(get("/api/moradores/" + outro.id()), usuario.token(), null).andExpect(status().isForbidden());
        enviar(get("/api/moradores/" + outro.id()), admin.token(), null).andExpect(status().isOk());
        enviar(delete("/api/moradores/" + outro.id()), usuario.token(), null).andExpect(status().isForbidden());
        enviar(delete("/api/moradores/" + outro.id()), admin.token(), null).andExpect(status().isNoContent());
    }

    @Test
    void crudDeMorador() throws Exception {
        Conta conta = novaConta(MOOCA);
        Conta admin = novoAdmin();
        enviar(get("/api/moradores/" + conta.id()), conta.token(), null).andExpect(status().isOk())
                .andExpect(jsonPath("$.distrito.id").value(MOOCA)).andExpect(jsonPath("$.senha").doesNotExist());
        enviar(put("/api/moradores/" + conta.id()), conta.token(), "{\"nome\":\"Novo Nome\",\"email\":\"novo-%d@example.com\"}".formatted(conta.id()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Novo Nome"));
        enviar(get("/api/moradores?distritoId=" + MOOCA), admin.token(), null).andExpect(status().isOk());
        enviar(delete("/api/moradores/" + conta.id()), admin.token(), null).andExpect(status().isNoContent());
        enviar(get("/api/moradores/" + conta.id()), admin.token(), null).andExpect(status().isNotFound());
    }

    @Test
    void crudDeChamado() throws Exception {
        Conta autor = novaConta(MOOCA);
        Conta outro = novaConta(MOOCA);
        long id = novoChamado(autor);
        enviar(get("/api/chamados/" + id), autor.token(), null).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ABERTO")).andExpect(jsonPath("$.distrito.id").value(MOOCA))
                .andExpect(jsonPath("$.moradorId").value(autor.id()));
        enviar(put("/api/chamados/" + id), outro.token(), "{\"servicoId\":3,\"referencia\":\"Outra referência\"}")
                .andExpect(status().isForbidden());
        enviar(put("/api/chamados/" + id), autor.token(), "{\"servicoId\":3,\"referencia\":\"Outra referência\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.servico.nome").value("Tapa-buraco"));
        enviar(get("/api/chamados?distritoId=" + MOOCA + "&status=ABERTO"), autor.token(), null).andExpect(status().isOk());
        enviar(delete("/api/chamados/" + id), autor.token(), null).andExpect(status().isNoContent());
        enviar(get("/api/chamados/" + id), autor.token(), null).andExpect(status().isNotFound());
    }

    @Test
    void duasConfirmacoesGeramGolUmaUnicaVez() throws Exception {
        Conta autor = novaConta(MOOCA);
        Conta marcou = novaConta(MOOCA);
        Conta vizinho1 = novaConta(MOOCA);
        Conta vizinho2 = novaConta(MOOCA);
        Conta vizinho3 = novaConta(MOOCA);
        long chamado = novoChamado(autor);

        votar(chamado, vizinho1, "CONFIRMA").andExpect(status().isUnprocessableEntity());
        resolver(chamado, marcou).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RESOLVIDO"));
        resolver(chamado, marcou).andExpect(status().isUnprocessableEntity());

        votar(chamado, vizinho1, "CONFIRMA").andExpect(status().isOk())
                .andExpect(jsonPath("$.desfecho").value("AGUARDANDO")).andExpect(jsonPath("$.confirmacoes").value(1));
        votar(chamado, vizinho2, "CONFIRMA").andExpect(status().isOk())
                .andExpect(jsonPath("$.desfecho").value("GOL")).andExpect(jsonPath("$.golsMarcados").value(GOLS_GUIAS));

        enviar(get("/api/chamados/" + chamado), autor.token(), null).andExpect(jsonPath("$.virouGol").value(true));
        enviar(get("/api/chamados/" + chamado + "/confirmacoes"), autor.token(), null).andExpect(jsonPath("$.length()").value(2));
        votar(chamado, vizinho3, "CONFIRMA").andExpect(status().isUnprocessableEntity());
        enviar(delete("/api/chamados/" + chamado), autor.token(), null).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void duasContestacoesReabremOChamado() throws Exception {
        Conta autor = novaConta(MOOCA);
        Conta marcou = novaConta(MOOCA);
        Conta vizinho1 = novaConta(MOOCA);
        Conta vizinho2 = novaConta(MOOCA);
        long chamado = novoChamado(autor);
        resolver(chamado, marcou).andExpect(status().isOk());

        votar(chamado, vizinho1, "CONTESTA").andExpect(jsonPath("$.desfecho").value("AGUARDANDO"));
        votar(chamado, vizinho2, "CONTESTA").andExpect(jsonPath("$.desfecho").value("ANULADO"));

        enviar(get("/api/chamados/" + chamado), autor.token(), null)
                .andExpect(jsonPath("$.status").value("ABERTO")).andExpect(jsonPath("$.virouGol").value(false));
        enviar(get("/api/chamados/" + chamado + "/confirmacoes"), autor.token(), null).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void regrasDoVarBloqueiamFraudes() throws Exception {
        Conta autor = novaConta(MOOCA);
        Conta marcou = novaConta(MOOCA);
        Conta vizinho = novaConta(MOOCA);
        Conta deFora = novaConta(TATUAPE);
        long chamado = novoChamado(autor);

        resolver(chamado, deFora).andExpect(status().isUnprocessableEntity());
        resolver(chamado, marcou).andExpect(status().isOk());

        votar(chamado, autor, "CONFIRMA").andExpect(status().isUnprocessableEntity());
        votar(chamado, marcou, "CONFIRMA").andExpect(status().isUnprocessableEntity());
        votar(chamado, deFora, "CONFIRMA").andExpect(status().isUnprocessableEntity());
        votar(chamado, vizinho, "CONFIRMA").andExpect(status().isOk());
        votar(chamado, vizinho, "CONFIRMA").andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Você já votou neste chamado."));
    }

    @Test
    void errosViramRespostasClaras() throws Exception {
        Conta conta = novaConta(MOOCA);
        Conta admin = novoAdmin();
        enviar(get("/api/chamados/999999"), conta.token(), null).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Chamado não encontrado."));
        enviar(get("/api/chamados/abc"), conta.token(), null).andExpect(status().isBadRequest());
        mvc.perform(get("/api/rodadas/99/partidas")).andExpect(status().isNotFound());
        enviar(get("/api/rota-que-nao-existe"), conta.token(), null).andExpect(status().isNotFound());
        enviar(put("/api/distritos"), conta.token(), null).andExpect(status().isMethodNotAllowed());
        enviar(post("/api/chamados"), conta.token(), "isto não é json").andExpect(status().isBadRequest());
        enviar(post("/api/chamados"), conta.token(), "{\"servicoId\":1,\"referencia\":\"abc\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0]").value("A referência deve ter de 5 a 120 caracteres."));
        enviar(post("/api/chamados"), conta.token(), "{\"servicoId\":999,\"referencia\":\"Trecho de teste\"}")
                .andExpect(status().isUnprocessableEntity());
        enviar(post("/api/moradores"), null, "{\"distritoId\":1,\"nome\":\"Teste\",\"email\":\"ana.ficticia@example.com\",\"senha\":\"%s\"}".formatted(SENHA))
                .andExpect(status().isConflict());
        enviar(post("/api/moradores"), null, "{\"distritoId\":1,\"nome\":\"Teste\",\"email\":\"invalido\",\"senha\":\"curta\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.length()").value(2));
        enviar(post("/api/moradores"), null, "{\"distritoId\":99,\"nome\":\"Teste\",\"email\":\"x-%s@example.com\",\"senha\":\"%s\"}".formatted(UUID.randomUUID(), SENHA))
                .andExpect(status().isUnprocessableEntity());
        enviar(delete("/api/moradores/1"), admin.token(), null).andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Este morador já participou do jogo e não pode ser excluído."));
    }

    @Test
    void corsLiberaSoAsOrigensConfiguradas() throws Exception {
        mvc.perform(get("/api/distritos").header("Origin", "https://varzea-flame.vercel.app"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://varzea-flame.vercel.app"));
        mvc.perform(get("/api/distritos").header("Origin", "https://site-qualquer.example.com"))
                .andExpect(status().isForbidden());
    }
}
