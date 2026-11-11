package br.com.fiap.varzea.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** Problema da rua relatado por um morador. Tabela T_VZ_CHAMADO. */
@Entity
@Table(name = "T_VZ_CHAMADO")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqChamado")
    @SequenceGenerator(name = "seqChamado", sequenceName = "SQ_VZ_CHAMADO", allocationSize = 1)
    @Column(name = "ID_CHAMADO")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_SERVICO")
    private Servico servico;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_DISTRITO")
    private Distrito distrito;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_MORADOR")
    private Morador morador;

    @NotBlank(message = "Informe uma referência.")
    @Size(min = 5, max = 120, message = "A referência deve ter de 5 a 120 caracteres.")
    @Column(name = "DS_REFERENCIA", nullable = false, length = 120)
    private String referencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_CHAMADO", nullable = false, length = 15)
    private StatusChamado status;

    @Column(name = "DT_ABERTURA", nullable = false, updatable = false)
    private LocalDate dataAbertura;

    @Column(name = "DT_RESOLUCAO")
    private LocalDate dataResolucao;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "ID_MORADOR_RESOLVEU")
    private Morador moradorResolveu;

    @Transient
    private Boolean virouGol;

    @PrePersist
    void aoCriar() {
        this.dataAbertura = LocalDate.now();
        this.status = StatusChamado.ABERTO;
    }

    public Long getMoradorId() {
        return morador == null ? null : morador.getId();
    }

    public Long getMoradorResolveuId() {
        return moradorResolveu == null ? null : moradorResolveu.getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public Distrito getDistrito() {
        return distrito;
    }

    public void setDistrito(Distrito distrito) {
        this.distrito = distrito;
    }

    public Morador getMorador() {
        return morador;
    }

    public void setMorador(Morador morador) {
        this.morador = morador;
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

    public Morador getMoradorResolveu() {
        return moradorResolveu;
    }

    public void setMoradorResolveu(Morador moradorResolveu) {
        this.moradorResolveu = moradorResolveu;
    }

    public Boolean getVirouGol() {
        return virouGol;
    }

    public void setVirouGol(Boolean virouGol) {
        this.virouGol = virouGol;
    }

}
