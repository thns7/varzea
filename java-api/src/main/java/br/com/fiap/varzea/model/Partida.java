package br.com.fiap.varzea.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/** Confronto entre dois distritos em uma rodada. O placar é calculado. Tabela T_VZ_PARTIDA. */
@Entity
@Table(name = "T_VZ_PARTIDA")
public class Partida {

    @Id
    @Column(name = "ID_PARTIDA")
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_RODADA")
    private Rodada rodada;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_DISTRITO_CASA")
    private Distrito casa;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_DISTRITO_FORA")
    private Distrito fora;

    @Transient
    private Long golsCasa;

    @Transient
    private Long golsFora;

    public Integer getRodadaId() {
        return rodada == null ? null : rodada.getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Rodada getRodada() {
        return rodada;
    }

    public void setRodada(Rodada rodada) {
        this.rodada = rodada;
    }

    public Distrito getCasa() {
        return casa;
    }

    public void setCasa(Distrito casa) {
        this.casa = casa;
    }

    public Distrito getFora() {
        return fora;
    }

    public void setFora(Distrito fora) {
        this.fora = fora;
    }

    public Long getGolsCasa() {
        return golsCasa;
    }

    public void setGolsCasa(Long golsCasa) {
        this.golsCasa = golsCasa;
    }

    public Long getGolsFora() {
        return golsFora;
    }

    public void setGolsFora(Long golsFora) {
        this.golsFora = golsFora;
    }

}
