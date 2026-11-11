package br.com.fiap.varzea.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/** Período de disputa. Tabela T_VZ_RODADA. */
@Entity
@Table(name = "T_VZ_RODADA")
public class Rodada {

    @Id
    @Column(name = "ID_RODADA")
    private Integer id;

    @Column(name = "NR_RODADA", nullable = false)
    private Integer numero;

    @Column(name = "DT_INICIO", nullable = false)
    private LocalDate inicio;

    @Column(name = "DT_FIM", nullable = false)
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
