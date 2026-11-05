package br.com.fiap.varzea.dao;

import br.com.fiap.varzea.model.Confirmacao;
import br.com.fiap.varzea.model.ResultadoVar;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/** Acesso à tabela T_VZ_CONFIRMACAO. */
@Repository
public class ConfirmacaoDao {

    private final JdbcTemplate jdbc;

    public ConfirmacaoDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Confirmacao> listarPorChamado(long chamadoId) {
        return jdbc.query("""
                SELECT id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao
                  FROM t_vz_confirmacao WHERE id_chamado = ? ORDER BY id_confirmacao
                """, (rs, n) -> {
            Confirmacao c = new Confirmacao();
            c.setId(rs.getLong("id_confirmacao"));
            c.setChamadoId(rs.getLong("id_chamado"));
            c.setMoradorId(rs.getLong("id_morador"));
            c.setResultado(ResultadoVar.valueOf(rs.getString("st_resultado")));
            c.setFotoUrl(rs.getString("ds_foto_url"));
            c.setData(rs.getDate("dt_confirmacao").toLocalDate());
            return c;
        }, chamadoId);
    }

    public void inserir(long chamadoId, long moradorId, ResultadoVar resultado, String fotoUrl, LocalDate data) {
        Long id = jdbc.queryForObject("SELECT sq_vz_confirmacao.NEXTVAL FROM dual", Long.class);
        jdbc.update("""
                INSERT INTO t_vz_confirmacao (id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, chamadoId, moradorId, resultado.name(), fotoUrl, Date.valueOf(data));
    }

    public void excluirPorChamado(long chamadoId) {
        jdbc.update("DELETE FROM t_vz_confirmacao WHERE id_chamado = ?", chamadoId);
    }
}
