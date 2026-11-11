package br.com.fiap.varzea.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

/** Gol validado de um chamado. Tabela T_VZ_GOL. */
@Entity
@Table(name = "T_VZ_GOL")
public class Gol {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqGol")
    @SequenceGenerator(name = "seqGol", sequenceName = "SQ_VZ_GOL", allocationSize = 1)
    @Column(name = "ID_GOL")
    private Long id;

    @JsonIgnore
    @OneToOne(optional = false)
    @JoinColumn(name = "ID_CHAMADO", unique = true)
    private Chamado chamado;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_RODADA")
    private Rodada rodada;

    @Column(name = "NR_GOLS", nullable = false)
    private Integer valor;

    @Column(name = "DT_VALIDACAO", nullable = false)
    private LocalDate dataValidacao;

    public Long getChamadoId() {
        return chamado == null ? null : chamado.getId();
    }

    public Integer getRodadaId() {
        return rodada == null ? null : rodada.getId();
    }

    public String getDistrito() {
        return chamado == null ? null : chamado.getDistrito().getNome();
    }

    public String getServico() {
        return chamado == null ? null : chamado.getServico().getNome();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Chamado getChamado() {
        return chamado;
    }

    public void setChamado(Chamado chamado) {
        this.chamado = chamado;
    }

    public Rodada getRodada() {
        return rodada;
    }

    public void setRodada(Rodada rodada) {
        this.rodada = rodada;
    }

    public Integer getValor() {
        return valor;
    }

    public void setValor(Integer valor) {
        this.valor = valor;
    }

    public LocalDate getDataValidacao() {
        return dataValidacao;
    }

    public void setDataValidacao(LocalDate dataValidacao) {
        this.dataValidacao = dataValidacao;
    }

}
