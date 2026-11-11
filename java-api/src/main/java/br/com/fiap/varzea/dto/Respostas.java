package br.com.fiap.varzea.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Corpos das respostas que não correspondem a uma única tabela. */
public final class Respostas {

    private Respostas() {
    }

    public record Token(String token, String tipo, Long moradorId, String perfil) {
    }

    /** Desfecho de um voto: AGUARDANDO, GOL ou ANULADO. */
    public record ResultadoVoto(String desfecho, long confirmacoes, long contestacoes, Integer golsMarcados) {
    }

    public record LinhaClassificacao(int posicao, int distritoId, String distrito, String zona, long golsValidados, long gols) {
    }

    public record Status(String api, String banco) {
    }

    /** Formato de erro da API, no padrão visto em aula: momento, status e lista de mensagens. */
    public record Erro(LocalDateTime timestamp, int status, String mensagem, List<String> errors) {
        public Erro(int status, String mensagem, List<String> errors) {
            this(LocalDateTime.now(), status, mensagem, errors);
        }
    }
}
