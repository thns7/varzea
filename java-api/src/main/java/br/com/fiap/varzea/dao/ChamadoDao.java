package br.com.fiap.varzea.dao;

import br.com.fiap.varzea.model.Chamado;
import br.com.fiap.varzea.model.StatusChamado;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso à tabela T_VZ_CHAMADO. */
@Repository
public class ChamadoDao {

    private static final String SELECT = """
            SELECT c.id_chamado, c.id_servico, s.nm_servico, s.nr_gols, c.id_distrito, d.nm_distrito, c.id_morador,
                   c.ds_referencia, c.st_chamado, c.dt_abertura, c.dt_resolucao, c.id_morador_resolveu,
                   (SELECT COUNT(*) FROM t_vz_gol g WHERE g.id_chamado = c.id_chamado) AS qt_gol
              FROM t_vz_chamado c
             INNER JOIN t_vz_servico s ON s.id_servico = c.id_servico
             INNER JOIN t_vz_distrito d ON d.id_distrito = c.id_distrito
            """;

    private final JdbcTemplate jdbc;

    public ChamadoDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Chamado> listar(Integer distritoId, StatusChamado status) {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (distritoId != null) {
            sql.append(" AND c.id_distrito = ?");
            parametros.add(distritoId);
        }
        if (status != null) {
            sql.append(" AND c.st_chamado = ?");
            parametros.add(status.name());
        }
        sql.append(" ORDER BY c.dt_abertura DESC, c.id_chamado DESC FETCH FIRST 100 ROWS ONLY");
        return jdbc.query(sql.toString(), this::paraChamado, parametros.toArray());
    }

    public Optional<Chamado> buscar(long id) {
        return jdbc.query(SELECT + " WHERE c.id_chamado = ?", this::paraChamado, id).stream().findFirst();
    }

    /** Trava a linha até o fim da transação, para dois votos simultâneos não gerarem dois gols. */
    public boolean travar(long id) {
        return !jdbc.queryForList("SELECT id_chamado FROM t_vz_chamado WHERE id_chamado = ? FOR UPDATE", Long.class, id).isEmpty();
    }

    public long inserir(int servicoId, int distritoId, long moradorId, String referencia, LocalDate abertura) {
        Long id = jdbc.queryForObject("SELECT sq_vz_chamado.NEXTVAL FROM dual", Long.class);
        jdbc.update("""
                INSERT INTO t_vz_chamado (id_chamado, id_servico, id_distrito, id_morador, ds_referencia, st_chamado, dt_abertura)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, id, servicoId, distritoId, moradorId, referencia, StatusChamado.ABERTO.name(), Date.valueOf(abertura));
        return id;
    }

    public void atualizar(long id, int servicoId, String referencia) {
        jdbc.update("UPDATE t_vz_chamado SET id_servico = ?, ds_referencia = ? WHERE id_chamado = ?", servicoId, referencia, id);
    }

    public void marcarResolvido(long id, long moradorId, LocalDate data) {
        jdbc.update("UPDATE t_vz_chamado SET st_chamado = ?, dt_resolucao = ?, id_morador_resolveu = ? WHERE id_chamado = ?",
                StatusChamado.RESOLVIDO.name(), Date.valueOf(data), moradorId, id);
    }

    public void reabrir(long id) {
        jdbc.update("UPDATE t_vz_chamado SET st_chamado = ?, dt_resolucao = NULL, id_morador_resolveu = NULL WHERE id_chamado = ?",
                StatusChamado.ABERTO.name(), id);
    }

    public void excluir(long id) {
        jdbc.update("DELETE FROM t_vz_chamado WHERE id_chamado = ?", id);
    }

    private Chamado paraChamado(ResultSet rs, int n) throws SQLException {
        Chamado c = new Chamado();
        c.setId(rs.getLong("id_chamado"));
        c.setServicoId(rs.getInt("id_servico"));
        c.setServicoNome(rs.getString("nm_servico"));
        c.setGols(rs.getInt("nr_gols"));
        c.setDistritoId(rs.getInt("id_distrito"));
        c.setDistritoNome(rs.getString("nm_distrito"));
        c.setMoradorId(rs.getLong("id_morador"));
        c.setReferencia(rs.getString("ds_referencia"));
        c.setStatus(StatusChamado.valueOf(rs.getString("st_chamado")));
        c.setDataAbertura(rs.getDate("dt_abertura").toLocalDate());
        Date resolucao = rs.getDate("dt_resolucao");
        c.setDataResolucao(resolucao == null ? null : resolucao.toLocalDate());
        long resolveu = rs.getLong("id_morador_resolveu");
        c.setMoradorResolveuId(rs.wasNull() ? null : resolveu);
        c.setVirouGol(rs.getInt("qt_gol") > 0);
        return c;
    }
}
