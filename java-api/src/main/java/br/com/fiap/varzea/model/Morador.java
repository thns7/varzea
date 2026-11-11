package br.com.fiap.varzea.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** Integrante da torcida de um distrito e usuário da API. Tabela T_VZ_MORADOR. */
@Entity
@Table(name = "T_VZ_MORADOR")
public class Morador {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqMorador")
    @SequenceGenerator(name = "seqMorador", sequenceName = "SQ_VZ_MORADOR", allocationSize = 1)
    @Column(name = "ID_MORADOR")
    private Long id;

    @NotNull(message = "Informe o distrito.")
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_DISTRITO")
    private Distrito distrito;

    @NotBlank(message = "Informe o nome.")
    @Size(max = 80, message = "O nome deve ter no máximo 80 caracteres.")
    @Column(name = "NM_MORADOR", nullable = false, length = 80)
    private String nome;

    @NotBlank(message = "Informe o e-mail.")
    @Email(message = "E-mail inválido.")
    @Column(name = "DS_EMAIL", nullable = false, length = 120, unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "DS_SENHA", nullable = false, length = 100)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "DS_PERFIL", nullable = false, length = 10)
    private Perfil perfil;

    @Column(name = "DT_CADASTRO", nullable = false, updatable = false)
    private LocalDate dataCadastro;

    @PrePersist
    void aoCriar() {
        this.dataCadastro = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Distrito getDistrito() {
        return distrito;
    }

    public void setDistrito(Distrito distrito) {
        this.distrito = distrito;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

}
