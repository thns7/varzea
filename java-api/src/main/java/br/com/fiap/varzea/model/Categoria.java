package br.com.fiap.varzea.model;

import jakarta.persistence.*;

/** Grupo de serviços. Tabela T_VZ_CATEGORIA. */
@Entity
@Table(name = "T_VZ_CATEGORIA")
public class Categoria {

    @Id
    @Column(name = "ID_CATEGORIA")
    private Integer id;

    @Column(name = "NM_CATEGORIA", nullable = false, length = 40)
    private String nome;

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

}
