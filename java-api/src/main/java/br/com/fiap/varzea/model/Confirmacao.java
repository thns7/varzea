package br.com.fiap.varzea.model;

import java.time.LocalDate;

/** Voto de um vizinho no VAR. Tabela T_VZ_CONFIRMACAO. */
public class Confirmacao {

    private Long id;
    private Long chamadoId;
    private Long moradorId;
    private ResultadoVar resultado;
    private String fotoUrl;
    private LocalDate data;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChamadoId() {
        return chamadoId;
    }

    public void setChamadoId(Long chamadoId) {
        this.chamadoId = chamadoId;
    }

    public Long getMoradorId() {
        return moradorId;
    }

    public void setMoradorId(Long moradorId) {
        this.moradorId = moradorId;
    }

    public ResultadoVar getResultado() {
        return resultado;
    }

    public void setResultado(ResultadoVar resultado) {
        this.resultado = resultado;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }
}
