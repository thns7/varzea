package br.com.fiap.varzea.model;

/** Confronto de uma rodada, com o placar calculado. Tabela T_VZ_PARTIDA. */
public class Partida {

    private Long id;
    private Integer rodadaId;
    private Integer casaId;
    private String casaNome;
    private Long golsCasa;
    private Integer foraId;
    private String foraNome;
    private Long golsFora;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRodadaId() {
        return rodadaId;
    }

    public void setRodadaId(Integer rodadaId) {
        this.rodadaId = rodadaId;
    }

    public Integer getCasaId() {
        return casaId;
    }

    public void setCasaId(Integer casaId) {
        this.casaId = casaId;
    }

    public String getCasaNome() {
        return casaNome;
    }

    public void setCasaNome(String casaNome) {
        this.casaNome = casaNome;
    }

    public Long getGolsCasa() {
        return golsCasa;
    }

    public void setGolsCasa(Long golsCasa) {
        this.golsCasa = golsCasa;
    }

    public Integer getForaId() {
        return foraId;
    }

    public void setForaId(Integer foraId) {
        this.foraId = foraId;
    }

    public String getForaNome() {
        return foraNome;
    }

    public void setForaNome(String foraNome) {
        this.foraNome = foraNome;
    }

    public Long getGolsFora() {
        return golsFora;
    }

    public void setGolsFora(Long golsFora) {
        this.golsFora = golsFora;
    }
}
