package br.com.fiap.varzea.model;

import java.time.LocalDate;

/** Problema da rua relatado por um morador. Tabela T_VZ_CHAMADO. */
public class Chamado {

    private Long id;
    private Integer servicoId;
    private String servicoNome;
    private Integer gols;
    private Integer distritoId;
    private String distritoNome;
    private Long moradorId;
    private String referencia;
    private StatusChamado status;
    private LocalDate dataAbertura;
    private LocalDate dataResolucao;
    private Long moradorResolveuId;
    private Boolean virouGol;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getServicoId() {
        return servicoId;
    }

    public void setServicoId(Integer servicoId) {
        this.servicoId = servicoId;
    }

    public String getServicoNome() {
        return servicoNome;
    }

    public void setServicoNome(String servicoNome) {
        this.servicoNome = servicoNome;
    }

    public Integer getGols() {
        return gols;
    }

    public void setGols(Integer gols) {
        this.gols = gols;
    }

    public Integer getDistritoId() {
        return distritoId;
    }

    public void setDistritoId(Integer distritoId) {
        this.distritoId = distritoId;
    }

    public String getDistritoNome() {
        return distritoNome;
    }

    public void setDistritoNome(String distritoNome) {
        this.distritoNome = distritoNome;
    }

    public Long getMoradorId() {
        return moradorId;
    }

    public void setMoradorId(Long moradorId) {
        this.moradorId = moradorId;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public StatusChamado getStatus() {
        return status;
    }

    public void setStatus(StatusChamado status) {
        this.status = status;
    }

    public LocalDate getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDate dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDate getDataResolucao() {
        return dataResolucao;
    }

    public void setDataResolucao(LocalDate dataResolucao) {
        this.dataResolucao = dataResolucao;
    }

    public Long getMoradorResolveuId() {
        return moradorResolveuId;
    }

    public void setMoradorResolveuId(Long moradorResolveuId) {
        this.moradorResolveuId = moradorResolveuId;
    }

    public Boolean getVirouGol() {
        return virouGol;
    }

    public void setVirouGol(Boolean virouGol) {
        this.virouGol = virouGol;
    }
}
