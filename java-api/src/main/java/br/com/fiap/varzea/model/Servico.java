package br.com.fiap.varzea.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Tipo de problema da rua e quantos gols vale. Tabela T_VZ_SERVICO. */
@Entity
@Table(name = "T_VZ_SERVICO")
public class Servico {

    @Id
    @Column(name = "ID_SERVICO")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_CATEGORIA")
    private Categoria categoria;

    @Column(name = "NM_SERVICO", nullable = false, length = 150)
    private String nome;

    @Column(name = "NR_GOLS", nullable = false)
    private Integer gols;

    @Column(name = "NR_MINUTOS", nullable = false)
    private Integer minutos;

    @Column(name = "VL_CHANCE_30D", nullable = false, precision = 4, scale = 3)
    private BigDecimal chance30d;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getGols() {
        return gols;
    }

    public void setGols(Integer gols) {
        this.gols = gols;
    }

    public Integer getMinutos() {
        return minutos;
    }

    public void setMinutos(Integer minutos) {
        this.minutos = minutos;
    }

    public BigDecimal getChance30d() {
        return chance30d;
    }

    public void setChance30d(BigDecimal chance30d) {
        this.chance30d = chance30d;
    }

}
