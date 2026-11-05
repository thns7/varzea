package br.com.fiap.varzea.model;

import java.math.BigDecimal;

/** Tipo de problema da rua e quantos gols vale. Tabela T_VZ_SERVICO. */
public class Servico {

    private Integer id;
    private Integer categoriaId;
    private String categoriaNome;
    private String nome;
    private Integer gols;
    private Integer minutos;
    private BigDecimal chance30d;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNome() {
        return categoriaNome;
    }

    public void setCategoriaNome(String categoriaNome) {
        this.categoriaNome = categoriaNome;
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
