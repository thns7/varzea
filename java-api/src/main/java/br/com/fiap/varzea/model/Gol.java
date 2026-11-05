package br.com.fiap.varzea.model;

import java.time.LocalDate;

/** Gol validado de um chamado. Tabela T_VZ_GOL. */
public class Gol {

    private Long id;
    private Long chamadoId;
    private Integer rodadaId;
    private Integer valor;
    private LocalDate dataValidacao;
    private String distritoNome;
    private String servicoNome;

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

    public Integer getRodadaId() {
        return rodadaId;
    }

    public void setRodadaId(Integer rodadaId) {
        this.rodadaId = rodadaId;
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

    public String getDistritoNome() {
        return distritoNome;
    }

    public void setDistritoNome(String distritoNome) {
        this.distritoNome = distritoNome;
    }

    public String getServicoNome() {
        return servicoNome;
    }

    public void setServicoNome(String servicoNome) {
        this.servicoNome = servicoNome;
    }
}
