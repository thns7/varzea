package br.com.fiap.varzea.model;

import jakarta.persistence.*;

/** Time do campeonato. Tabela T_VZ_DISTRITO. */
@Entity
@Table(name = "T_VZ_DISTRITO")
public class Distrito {

    @Id
    @Column(name = "ID_DISTRITO")
    private Integer id;

    @Column(name = "NM_DISTRITO", nullable = false, length = 60)
    private String nome;

    @Column(name = "NM_ZONA", nullable = false, length = 20)
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
