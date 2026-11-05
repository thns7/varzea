package br.com.fiap.varzea;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testa a API inteira, de ponta a ponta, sobre os scripts reais do banco. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VarzeaApiTest {

    private static final int MOOCA = 1;
    private static final int TATUAPE = 2;
    private static final int GUIAS = 1;
    private static final int GOLS_GUIAS = 8;
    private static final String FOTO = "https://example.com/foto.jpg";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private ResultActions postar(String url, String corpo) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private long novoMorador(int distrito) throws Exception {
        String email = "teste-" + UUID.randomUUID() + "@example.com";
        String corpo = postar("/api/moradores", "{\"distritoId\":%d,\"nome\":\"Teste\",\"email\":\"%s\"}".formatted(distrito, email))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(corpo).get("id").asLong();
    }

    private long novoChamado(long morador) throws Exception {
        String corpo = postar("/api/chamados", "{\"servicoId\":%d,\"moradorId\":%d,\"referencia\":\"Trecho de teste\"}".formatted(GUIAS, morador))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(corpo).get("id").asLong();
    }

    private ResultActions resolver(long chamado, long morador) throws Exception {
        return postar("/api/chamados/" + chamado + "/resolver", "{\"moradorId\":%d}".formatted(morador));
    }

    private ResultActions votar(long chamado, long morador, String resultado) throws Exception {
        return postar("/api/chamados/" + chamado + "/confirmacoes",
                "{\"moradorId\":%d,\"resultado\":\"%s\",\"fotoUrl\":\"%s\"}".formatted(morador, resultado, FOTO));
    }

    @Test
    void scriptsDoBancoCarregamEConsultasRespondem() throws Exception {
        mvc.perform(get("/api/status")).andExpect(status().isOk()).andExpect(jsonPath("$.banco").value("conectado"));
        mvc.perform(get("/api/distritos")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(6));
        mvc.perform(get("/api/categorias")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(6));
        mvc.perform(get("/api/servicos")).andExpect(status().isOk()).andExpect(jsonPath("$[0].gols").value(GOLS_GUIAS));
        mvc.perform(get("/api/servicos/1")).andExpect(status().isOk()).andExpect(jsonPath("$.categoriaNome").value("Pavimento"));
        mvc.perform(get("/api/rodadas")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(5));
        mvc.perform(get("/api/rodadas/1/partidas")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].casaNome").value("MOOCA")).andExpect(jsonPath("$[0].golsCasa").value(8))
                .andExpect(jsonPath("$[0].foraNome").value("TATUAPE")).andExpect(jsonPath("$[0].golsFora").value(2));
        mvc.perform(get("/api/gols")).andExpect(status().isOk());
        mvc.perform(get("/api/classificacao")).andExpect(status().isOk()).andExpect(jsonPath("$[0].posicao").value(1));
        mvc.perform(get("/api/chamados/1")).andExpect(status().isOk()).andExpect(jsonPath("$.virouGol").value(true));
        mvc.perform(get("/api/chamados?distritoId=2&status=CANCELADO")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(12));
    }

    @Test
    void crudDeMorador() throws Exception {
        long id = novoMorador(MOOCA);
        mvc.perform(get("/api/moradores/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.distritoId").value(MOOCA));
        mvc.perform(put("/api/moradores/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Novo Nome\",\"email\":\"novo-%d@example.com\"}".formatted(id)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Novo Nome"));
        mvc.perform(get("/api/moradores?distritoId=" + MOOCA)).andExpect(status().isOk());
        mvc.perform(delete("/api/moradores/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/moradores/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void crudDeChamado() throws Exception {
        long morador = novoMorador(MOOCA);
        long id = novoChamado(morador);
        mvc.perform(get("/api/chamados/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ABERTO")).andExpect(jsonPath("$.distritoId").value(MOOCA));
        mvc.perform(put("/api/chamados/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servicoId\":3,\"referencia\":\"Outra referência\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.servicoNome").value("Tapa-buraco"));
        mvc.perform(delete("/api/chamados/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/chamados/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void duasConfirmacoesGeramGolUmaUnicaVez() throws Exception {
        long autor = novoMorador(MOOCA);
        long marcou = novoMorador(MOOCA);
        long vizinho1 = novoMorador(MOOCA);
        long vizinho2 = novoMorador(MOOCA);
        long vizinho3 = novoMorador(MOOCA);
        long chamado = novoChamado(autor);

        votar(chamado, vizinho1, "CONFIRMA").andExpect(status().isUnprocessableEntity());
        resolver(chamado, marcou).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RESOLVIDO"));
        resolver(chamado, marcou).andExpect(status().isUnprocessableEntity());

        votar(chamado, vizinho1, "CONFIRMA").andExpect(status().isOk())
                .andExpect(jsonPath("$.desfecho").value("AGUARDANDO")).andExpect(jsonPath("$.confirmacoes").value(1));
        votar(chamado, vizinho2, "CONFIRMA").andExpect(status().isOk())
                .andExpect(jsonPath("$.desfecho").value("GOL")).andExpect(jsonPath("$.golsMarcados").value(GOLS_GUIAS));

        mvc.perform(get("/api/chamados/" + chamado)).andExpect(jsonPath("$.virouGol").value(true));
        mvc.perform(get("/api/chamados/" + chamado + "/confirmacoes")).andExpect(jsonPath("$.length()").value(2));
        votar(chamado, vizinho3, "CONFIRMA").andExpect(status().isUnprocessableEntity());
        mvc.perform(delete("/api/chamados/" + chamado)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void duasContestacoesReabremOChamado() throws Exception {
        long autor = novoMorador(MOOCA);
        long marcou = novoMorador(MOOCA);
        long vizinho1 = novoMorador(MOOCA);
        long vizinho2 = novoMorador(MOOCA);
        long chamado = novoChamado(autor);
        resolver(chamado, marcou).andExpect(status().isOk());

        votar(chamado, vizinho1, "CONTESTA").andExpect(jsonPath("$.desfecho").value("AGUARDANDO"));
        votar(chamado, vizinho2, "CONTESTA").andExpect(jsonPath("$.desfecho").value("ANULADO"));

        mvc.perform(get("/api/chamados/" + chamado)).andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.virouGol").value(false));
        mvc.perform(get("/api/chamados/" + chamado + "/confirmacoes")).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void regrasDoVarBloqueiamFraudes() throws Exception {
        long autor = novoMorador(MOOCA);
        long marcou = novoMorador(MOOCA);
        long vizinho = novoMorador(MOOCA);
        long deFora = novoMorador(TATUAPE);
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
        mvc.perform(get("/api/chamados/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Chamado não encontrado."));
        mvc.perform(get("/api/chamados/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/rota-que-nao-existe")).andExpect(status().isNotFound());
        mvc.perform(put("/api/distritos")).andExpect(status().isMethodNotAllowed());
        postar("/api/chamados", "isto não é json").andExpect(status().isBadRequest());
        postar("/api/chamados", "{\"servicoId\":1,\"moradorId\":1,\"referencia\":\"abc\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detalhes[0]").exists());
        postar("/api/chamados", "{\"servicoId\":999,\"moradorId\":1,\"referencia\":\"Trecho de teste\"}")
                .andExpect(status().isUnprocessableEntity());
        postar("/api/chamados", "{\"servicoId\":1,\"moradorId\":999999,\"referencia\":\"Trecho de teste\"}")
                .andExpect(status().isUnprocessableEntity());
        postar("/api/moradores", "{\"distritoId\":1,\"nome\":\"Teste\",\"email\":\"ana.ficticia@example.com\"}")
                .andExpect(status().isConflict());
        postar("/api/moradores", "{\"distritoId\":1,\"nome\":\"Teste\",\"email\":\"invalido\"}").andExpect(status().isBadRequest());
        // Morador com chamados não pode ser excluído, e a resposta não vaza SQL.
        String corpo = mvc.perform(delete("/api/moradores/1")).andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString();
        JsonNode erro = json.readTree(corpo);
        assertEquals("A operação conflita com dados já registrados.", erro.get("mensagem").asText());
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
