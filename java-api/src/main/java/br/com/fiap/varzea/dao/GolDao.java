package br.com.fiap.varzea.dao;

import br.com.fiap.varzea.model.Gol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Acesso à tabela T_VZ_GOL. */
public interface GolDao extends JpaRepository<Gol, Long> {

    boolean existsByChamado_Id(Long chamadoId);

    List<Gol> findAllByOrderByDataValidacaoDescIdDesc();

    @Query("""
            select coalesce(sum(g.valor), 0) from Gol g
             where g.rodada.id = :rodadaId and g.chamado.distrito.id = :distritoId
            """)
    Long somarGols(@Param("rodadaId") Integer rodadaId, @Param("distritoId") Integer distritoId);

    /** Linhas com: id, nome e zona do distrito, quantidade de gols validados e soma dos gols. */
    @Query("""
            select d.id, d.nome, d.zona, count(g.id), coalesce(sum(g.valor), 0)
              from Distrito d
              left join Chamado c on c.distrito = d
              left join Gol g on g.chamado = c
             group by d.id, d.nome, d.zona
             order by coalesce(sum(g.valor), 0) desc, d.nome
            """)
    List<Object[]> classificacao();
}
