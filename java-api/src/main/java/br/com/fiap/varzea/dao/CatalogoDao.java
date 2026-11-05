package br.com.fiap.varzea.dao;

import br.com.fiap.varzea.model.Categoria;
import br.com.fiap.varzea.model.Distrito;
import br.com.fiap.varzea.model.Servico;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Acesso às tabelas de consulta: distritos, categorias e serviços. */
@Repository
public class CatalogoDao {

    private static final String SELECT_SERVICO = """
            SELECT s.id_servico, s.id_categoria, c.nm_categoria, s.nm_servico, s.nr_gols, s.nr_minutos, s.vl_chance_30d
              FROM t_vz_servico s
             INNER JOIN t_vz_categoria c ON c.id_categoria = s.id_categoria
            """;

    private final JdbcTemplate jdbc;

    public CatalogoDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Distrito> listarDistritos() {
        return jdbc.query("SELECT id_distrito, nm_distrito, nm_zona FROM t_vz_distrito ORDER BY nm_distrito", (rs, n) -> {
            Distrito d = new Distrito();
            d.setId(rs.getInt("id_distrito"));
            d.setNome(rs.getString("nm_distrito"));
            d.setZona(rs.getString("nm_zona"));
            return d;
        });
    }

    public boolean distritoExiste(int id) {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM t_vz_distrito WHERE id_distrito = ?", Integer.class, id);
        return total != null && total > 0;
    }

    public List<Categoria> listarCategorias() {
        return jdbc.query("SELECT id_categoria, nm_categoria FROM t_vz_categoria ORDER BY nm_categoria", (rs, n) -> {
            Categoria c = new Categoria();
            c.setId(rs.getInt("id_categoria"));
            c.setNome(rs.getString("nm_categoria"));
            return c;
        });
    }

    public List<Servico> listarServicos() {
        return jdbc.query(SELECT_SERVICO + " ORDER BY s.nr_gols DESC, s.nm_servico", this::paraServico);
    }

    public Optional<Servico> buscarServico(int id) {
        return jdbc.query(SELECT_SERVICO + " WHERE s.id_servico = ?", this::paraServico, id).stream().findFirst();
    }

    private Servico paraServico(java.sql.ResultSet rs, int n) throws java.sql.SQLException {
        Servico s = new Servico();
        s.setId(rs.getInt("id_servico"));
        s.setCategoriaId(rs.getInt("id_categoria"));
        s.setCategoriaNome(rs.getString("nm_categoria"));
        s.setNome(rs.getString("nm_servico"));
        s.setGols(rs.getInt("nr_gols"));
        s.setMinutos(rs.getInt("nr_minutos"));
        s.setChance30d(rs.getBigDecimal("vl_chance_30d"));
        return s;
    }
}
