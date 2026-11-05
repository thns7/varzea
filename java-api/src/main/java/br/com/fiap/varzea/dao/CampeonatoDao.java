package br.com.fiap.varzea.dao;

import br.com.fiap.varzea.dto.Respostas.LinhaClassificacao;
import br.com.fiap.varzea.model.Gol;
import br.com.fiap.varzea.model.Partida;
import br.com.fiap.varzea.model.Rodada;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Acesso às tabelas T_VZ_RODADA, T_VZ_PARTIDA e T_VZ_GOL. */
@Repository
public class CampeonatoDao {

    private final JdbcTemplate jdbc;

    public CampeonatoDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Rodada> listarRodadas() {
        return jdbc.query("SELECT id_rodada, nr_rodada, dt_inicio, dt_fim FROM t_vz_rodada ORDER BY nr_rodada", this::paraRodada);
    }

    /** A rodada vigente na data, ou a última já iniciada. */
    public Optional<Rodada> rodadaEm(LocalDate data) {
        return jdbc.query("""
                SELECT id_rodada, nr_rodada, dt_inicio, dt_fim FROM t_vz_rodada
                 WHERE dt_inicio <= ? ORDER BY nr_rodada DESC FETCH FIRST 1 ROWS ONLY
                """, this::paraRodada, Date.valueOf(data)).stream().findFirst();
    }

    public List<Partida> listarPartidas(int rodadaId) {
        return jdbc.query("""
                SELECT p.id_partida, p.id_rodada,
                       p.id_distrito_casa, dc.nm_distrito AS nm_casa,
                       (SELECT NVL(SUM(g.nr_gols), 0) FROM t_vz_gol g
                         INNER JOIN t_vz_chamado c ON c.id_chamado = g.id_chamado
                         WHERE g.id_rodada = p.id_rodada AND c.id_distrito = p.id_distrito_casa) AS gols_casa,
                       p.id_distrito_fora, df.nm_distrito AS nm_fora,
                       (SELECT NVL(SUM(g.nr_gols), 0) FROM t_vz_gol g
                         INNER JOIN t_vz_chamado c ON c.id_chamado = g.id_chamado
                         WHERE g.id_rodada = p.id_rodada AND c.id_distrito = p.id_distrito_fora) AS gols_fora
                  FROM t_vz_partida p
                 INNER JOIN t_vz_distrito dc ON dc.id_distrito = p.id_distrito_casa
                 INNER JOIN t_vz_distrito df ON df.id_distrito = p.id_distrito_fora
                 WHERE p.id_rodada = ?
                 ORDER BY p.id_partida
                """, (rs, n) -> {
            Partida p = new Partida();
            p.setId(rs.getLong("id_partida"));
            p.setRodadaId(rs.getInt("id_rodada"));
            p.setCasaId(rs.getInt("id_distrito_casa"));
            p.setCasaNome(rs.getString("nm_casa"));
            p.setGolsCasa(rs.getLong("gols_casa"));
            p.setForaId(rs.getInt("id_distrito_fora"));
            p.setForaNome(rs.getString("nm_fora"));
            p.setGolsFora(rs.getLong("gols_fora"));
            return p;
        }, rodadaId);
    }

    public List<Gol> listarGols() {
        return jdbc.query("""
                SELECT g.id_gol, g.id_chamado, g.id_rodada, g.nr_gols, g.dt_validacao, d.nm_distrito, s.nm_servico
                  FROM t_vz_gol g
                 INNER JOIN t_vz_chamado c ON c.id_chamado = g.id_chamado
                 INNER JOIN t_vz_distrito d ON d.id_distrito = c.id_distrito
                 INNER JOIN t_vz_servico s ON s.id_servico = c.id_servico
                 ORDER BY g.dt_validacao DESC, g.id_gol DESC
                """, (rs, n) -> {
            Gol g = new Gol();
            g.setId(rs.getLong("id_gol"));
            g.setChamadoId(rs.getLong("id_chamado"));
            g.setRodadaId(rs.getInt("id_rodada"));
            g.setValor(rs.getInt("nr_gols"));
            g.setDataValidacao(rs.getDate("dt_validacao").toLocalDate());
            g.setDistritoNome(rs.getString("nm_distrito"));
            g.setServicoNome(rs.getString("nm_servico"));
            return g;
        });
    }

    public void inserirGol(long chamadoId, int rodadaId, int valor, LocalDate data) {
        Long id = jdbc.queryForObject("SELECT sq_vz_gol.NEXTVAL FROM dual", Long.class);
        jdbc.update("INSERT INTO t_vz_gol (id_gol, id_chamado, id_rodada, nr_gols, dt_validacao) VALUES (?, ?, ?, ?, ?)",
                id, chamadoId, rodadaId, valor, Date.valueOf(data));
    }

    public List<LinhaClassificacao> classificacao() {
        return jdbc.query("""
                SELECT d.id_distrito, d.nm_distrito, d.nm_zona, COUNT(g.id_gol) AS qt_gols, NVL(SUM(g.nr_gols), 0) AS total_gols
                  FROM t_vz_distrito d
                  LEFT OUTER JOIN t_vz_chamado c ON c.id_distrito = d.id_distrito
                  LEFT OUTER JOIN t_vz_gol g ON g.id_chamado = c.id_chamado
                 GROUP BY d.id_distrito, d.nm_distrito, d.nm_zona
                 ORDER BY total_gols DESC, d.nm_distrito
                """, (rs, n) -> new LinhaClassificacao(n + 1, rs.getInt("id_distrito"), rs.getString("nm_distrito"),
                rs.getString("nm_zona"), rs.getLong("qt_gols"), rs.getLong("total_gols")));
    }

    private Rodada paraRodada(ResultSet rs, int n) throws SQLException {
        Rodada r = new Rodada();
        r.setId(rs.getInt("id_rodada"));
        r.setNumero(rs.getInt("nr_rodada"));
        r.setInicio(rs.getDate("dt_inicio").toLocalDate());
        r.setFim(rs.getDate("dt_fim").toLocalDate());
        return r;
    }
}
