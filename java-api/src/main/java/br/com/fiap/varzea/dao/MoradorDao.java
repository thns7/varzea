package br.com.fiap.varzea.dao;

import br.com.fiap.varzea.model.Morador;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Acesso à tabela T_VZ_MORADOR. */
@Repository
public class MoradorDao {

    private static final String SELECT = "SELECT id_morador, id_distrito, nm_morador, ds_email, dt_cadastro FROM t_vz_morador";

    private final JdbcTemplate jdbc;

    public MoradorDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Morador> listar(Integer distritoId) {
        if (distritoId == null) {
            return jdbc.query(SELECT + " ORDER BY nm_morador", this::paraMorador);
        }
        return jdbc.query(SELECT + " WHERE id_distrito = ? ORDER BY nm_morador", this::paraMorador, distritoId);
    }

    public Optional<Morador> buscar(long id) {
        return jdbc.query(SELECT + " WHERE id_morador = ?", this::paraMorador, id).stream().findFirst();
    }

    public long inserir(int distritoId, String nome, String email, LocalDate cadastro) {
        Long id = jdbc.queryForObject("SELECT sq_vz_morador.NEXTVAL FROM dual", Long.class);
        jdbc.update("INSERT INTO t_vz_morador (id_morador, id_distrito, nm_morador, ds_email, dt_cadastro) VALUES (?, ?, ?, ?, ?)",
                id, distritoId, nome, email, Date.valueOf(cadastro));
        return id;
    }

    public void atualizar(long id, String nome, String email) {
        jdbc.update("UPDATE t_vz_morador SET nm_morador = ?, ds_email = ? WHERE id_morador = ?", nome, email, id);
    }

    public void excluir(long id) {
        jdbc.update("DELETE FROM t_vz_morador WHERE id_morador = ?", id);
    }

    private Morador paraMorador(ResultSet rs, int n) throws SQLException {
        Morador m = new Morador();
        m.setId(rs.getLong("id_morador"));
        m.setDistritoId(rs.getInt("id_distrito"));
        m.setNome(rs.getString("nm_morador"));
        m.setEmail(rs.getString("ds_email"));
        m.setDataCadastro(rs.getDate("dt_cadastro").toLocalDate());
        return m;
    }
}
