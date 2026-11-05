package br.com.fiap.varzea.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Corpos das respostas que não correspondem a uma única tabela. */
public final class Respostas {

    private Respostas() {
    }

    /** Desfecho de um voto: AGUARDANDO, GOL ou ANULADO. */
    public record ResultadoVoto(String desfecho, long confirmacoes, long contestacoes, Integer golsMarcados) {
    }

    public record LinhaClassificacao(int posicao, int distritoId, String distrito, String zona, long golsValidados, long gols) {
    }

    public record Status(String api, String banco) {
    }

    public record Erro(LocalDateTime momento, int status, String mensagem, List<String> detalhes) {
        public Erro(int status, String mensagem, List<String> detalhes) {
            this(LocalDateTime.now(), status, mensagem, detalhes);
        }
    }
}
