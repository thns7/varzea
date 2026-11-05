package br.com.fiap.varzea.model;

import java.time.LocalDate;

/** Período de disputa. Tabela T_VZ_RODADA. */
public class Rodada {

    private Integer id;
    private Integer numero;
    private LocalDate inicio;
    private LocalDate fim;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public void setInicio(LocalDate inicio) {
        this.inicio = inicio;
    }

    public LocalDate getFim() {
        return fim;
    }

    public void setFim(LocalDate fim) {
        this.fim = fim;
    }
}
