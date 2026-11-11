package br.com.fiap.varzea.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

/** Voto de um vizinho no VAR da torcida. Tabela T_VZ_CONFIRMACAO. */
@Entity
@Table(name = "T_VZ_CONFIRMACAO")
public class Confirmacao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqConfirmacao")
    @SequenceGenerator(name = "seqConfirmacao", sequenceName = "SQ_VZ_CONFIRMACAO", allocationSize = 1)
    @Column(name = "ID_CONFIRMACAO")
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_CHAMADO")
    private Chamado chamado;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_MORADOR")
    private Morador morador;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_RESULTADO", nullable = false, length = 10)
    private ResultadoVar resultado;

    @Column(name = "DS_FOTO_URL", nullable = false, length = 200)
    private String fotoUrl;

    @Column(name = "DT_CONFIRMACAO", nullable = false)
    private LocalDate data;

    public Long getChamadoId() {
        return chamado == null ? null : chamado.getId();
    }

    public Long getMoradorId() {
        return morador == null ? null : morador.getId();
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

    public Morador getMorador() {
        return morador;
    }

    public void setMorador(Morador morador) {
        this.morador = morador;
    }

    public ResultadoVar getResultado() {
        return resultado;
    }

    public void setResultado(ResultadoVar resultado) {
        this.resultado = resultado;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

}
