package br.com.fiap.varzea.model;

/** Time do campeonato. Tabela T_VZ_DISTRITO. */
public class Distrito {

    private Integer id;
    private String nome;
    private String zona;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }
}
